/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.core.services.impl;

import static java.net.HttpURLConnection.HTTP_MULT_CHOICE;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_PAYMENT_REQUIRED;
import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import com.dytech.edge.common.Constants;
import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.common.Check;
import com.tle.common.NameValue;
import com.tle.core.guice.Bind;
import com.tle.core.services.HttpService;
import com.tle.core.services.http.Request;
import com.tle.core.services.http.Response;

/**
 * @author aholland
 */
@SuppressWarnings("nls")
@NonNullByDefault
@Bind(HttpService.class)
@Singleton
public class HttpServiceImpl implements HttpService
{
	// FIXME: remove it and use a HttpSession
	private static final Cache<String, Cookies> COOKIE_CACHE = CacheBuilder.newBuilder().softValues()
		.expireAfterAccess(30, TimeUnit.MINUTES).build();

	private static final Logger LOGGER = Logger.getLogger(HttpService.class);

	private final PoolingClientConnectionManager conMan;

	@Inject(optional = true)
	@Named("can.access.internet")
	private boolean canAccessInternet = true;

	public HttpServiceImpl() throws NoSuchAlgorithmException, KeyManagementException
	{
		X509TrustManager trustManager = new X509TrustManager()
		{
			final X509Certificate[] acceptedIssuers = new X509Certificate[]{};

			@Override
			public X509Certificate[] getAcceptedIssuers()
			{
				return acceptedIssuers;
			}

			@Override
			public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException
			{
				// Nothing to do here
			}

			@Override
			public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException
			{
				// Nothing to do here
			}
		};

		SSLContext context = SSLContext.getInstance("TLS");
		context.init(null, new TrustManager[]{trustManager}, new SecureRandom());

		SSLSocketFactory socketFactory = new SSLSocketFactory(context, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

		conMan = new PoolingClientConnectionManager();
		conMan.getSchemeRegistry().register(new Scheme("https", 443, socketFactory));
		conMan.setMaxTotal(10000);
		conMan.setDefaultMaxPerRoute(1000);
	}

	@Override
	public boolean canAccessInternet()
	{
		return canAccessInternet;
	}

	@Override
	public Response getWebContent(Request request, @Nullable ProxyDetails proxy)
	{
		return getWebContentPrivate(request, proxy, true);
	}

	@Override
	public Response getWebContent(Request request, @Nullable ProxyDetails proxy, boolean followRedirects)
	{
		return getWebContentPrivate(request, proxy, followRedirects);
	}

	private ResponseImpl getWebContentPrivate(Request request, @Nullable ProxyDetails proxy, boolean followRedirects)
	{
		final String url = request.getUrl();
		try
		{
			@SuppressWarnings("unused")
			URL u = new URL(url); // NOSONAR
		}
		catch( MalformedURLException ex )
		{
			return new ResponseImpl(HttpServletResponse.SC_BAD_REQUEST, "Invalid URL: " + url);
		}
		HttpRequestBase httpMethod = null;
		try
		{
			httpMethod = getHttpMethod(request);
			if( httpMethod == null )
			{
				return new ResponseImpl(HttpServletResponse.SC_METHOD_NOT_ALLOWED,
					"Only GET, POST, HEAD, PUT, DELETE and OPTIONS methods are supported");
			}
			if( !followRedirects )
			{
				HttpClientParams.setRedirecting(httpMethod.getParams(), false);
			}

			final DefaultHttpClient client = createClient(httpMethod.getURI().getScheme().equals("https"));

			if( proxy != null && proxy.isConfigured() )
			{
				final URI uri = httpMethod.getURI();
				final String host = uri.getHost();
				if( !proxy.isHostExcepted(host) )
				{
					final HttpHost proxyHost = new HttpHost(proxy.getHost(), proxy.getPort());
					client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxyHost);
					if( !Check.isEmpty(proxy.getUsername()) )
					{
						client.getCredentialsProvider().setCredentials(
							new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
							new UsernamePasswordCredentials(proxy.getUsername(), proxy.getPassword()));

					}
				}
			}

			// TODO: see fixme about cookie cache
			final String cacheKey = /* req.getSession().getId() */"FIXME" + ':' + url;
			Cookies cookies = COOKIE_CACHE.getIfPresent(cacheKey);
			if( cookies == null )
			{
				cookies = new Cookies();
				COOKIE_CACHE.put(cacheKey, cookies);
			}

			final HttpResponse response = exec(client, httpMethod, cookies);
			return new ResponseImpl(response, httpMethod);
		}
		catch( Exception e )
		{
			throw new RuntimeException(e);
		}
	}

	private DefaultHttpClient createClient(boolean https)
	{
		final DefaultHttpClient client = (https ? new DefaultHttpClient(conMan) : new DefaultHttpClient());
		// Allows a slightly lenient cookie acceptance
		client.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY);
		// Allows follow of redirects on POST
		client.setRedirectStrategy(new LaxRedirectStrategy());
		return client;
	}

	private HttpResponse exec(HttpClient client, HttpRequestBase method, Cookies cookies) throws Exception
	{
		addCookies(method, cookies);

		LOGGER.debug(method.getMethod() + "ing " + method.getURI());

		final HttpResponse response = client.execute(method);
		extractCookies(method.getURI(), response, cookies);

		return response;
	}

	private void extractCookies(URI uri, HttpResponse response, Cookies cookies) throws Exception
	{
		Header[] headers = response.getHeaders("Set-Cookie");
		for( Header header : headers )
		{
			ACookie cookie = new ACookie(uri, header.getValue());
			cookies.recvd.add(cookie);
			cookies.send.add(cookie);
		}
	}

	private void addCookies(HttpRequestBase method, Cookies cookies)
	{
		for( ACookie cookie : cookies.send )
		{
			method.addHeader("Cookie", cookie.toString());
		}
	}

	public static class Cookies
	{
		Set<ACookie> recvd = new HashSet<ACookie>();
		Set<ACookie> send = new HashSet<ACookie>();
	}

	// @NonNullByDefault(false)
	public static class ACookie
	{
		private final DateFormat expiresFormat1 = new SimpleDateFormat("E, dd MMM yyyy k:m:s 'GMT'", Locale.US);
		private final DateFormat expiresFormat2 = new SimpleDateFormat("E, dd-MMM-yyyy k:m:s 'GMT'", Locale.US);

		private final String name;
		private final String value;
		private final URI uri;
		@Nullable
		private Date expires;
		private String path;

		public ACookie(URI uri, String header)
		{
			String attributes[] = header.split(";");
			String nameValue = attributes[0].trim();
			this.uri = uri;
			this.name = nameValue.substring(0, nameValue.indexOf('='));
			this.value = nameValue.substring(nameValue.indexOf('=') + 1);
			this.path = "/";

			for( int i = 1; i < attributes.length; i++ )
			{
				nameValue = attributes[i].trim();
				int equals = nameValue.indexOf('=');
				if( equals == -1 )
				{
					continue;
				}
				String locName = nameValue.substring(0, equals);
				String locValue = nameValue.substring(equals + 1);
				if( locName.equalsIgnoreCase("path") )
				{
					this.path = locValue;
				}
				else if( locName.equalsIgnoreCase("expires") )
				{
					try
					{
						this.expires = expiresFormat1.parse(locValue);
					}
					catch( ParseException e )
					{
						try
						{
							this.expires = expiresFormat2.parse(locValue);
						}
						catch( ParseException e2 )
						{
							throw new IllegalArgumentException("Bad date format in header: " + locValue);
						}
					}
				}
			}
		}

		public boolean hasExpired()
		{
			if( expires == null )
			{
				return false;
			}
			Date now = new Date();
			return now.after(expires);
		}

		public String getName()
		{
			return name;
		}

		public URI getURI()
		{
			return uri;
		}

		public String getValue()
		{
			return value;
		}

		public boolean matches(URI uri)
		{
			if( hasExpired() )
			{
				return false;
			}

			String locPath = uri.getPath();
			if( locPath == null )
			{
				locPath = "/";
			}

			return locPath.startsWith(this.path);
		}

		@Override
		public String toString()
		{
			StringBuilder result = new StringBuilder(name);
			result.append("=");
			result.append(value);
			return result.toString();
		}
	}

	/**
	 * @param url
	 * @param method
	 * @param params
	 * @param postHtml If method == post and postHtml == true, then set the
	 *            content type to 'application/x-www-form-urlencoded;
	 *            charset=UTF-8'
	 * @return null if the method can't be handled
	 */
	@SuppressWarnings("deprecation")
	@Nullable
	private HttpRequestBase getHttpMethod(Request request)
	{
		final List<Header> convHeaders = Lists.transform(request.getHeaders(), new HeaderConverter());
		final List<NameValuePair> reqParams = Lists.transform(request.getParams(), new ParamConverter());
		List<NameValuePair> convParams = Lists.newArrayList();
		for( NameValuePair p : reqParams )
		{
			if( !p.getName().equals("url") )
			{
				convParams.add(p);
			}
		}

		final HttpRequestBase req;
		switch( request.getMethod() )
		{
			case GET:
				req = createGET(request.getUrl(), convParams);
				break;

			case POST:
				req = createPOST(request.getUrl(), convParams);
				String mimeType = request.getMimeType();
				if( mimeType == null )
				{
					mimeType = ContentType.APPLICATION_JSON.getMimeType();
				}
				String charset = request.getCharset();
				if( charset == null )
				{
					charset = ContentType.APPLICATION_JSON.getCharset().name();
				}
				try
				{
					((HttpPost) req).setEntity(new StringEntity(request.getBody(), mimeType, charset));
				}
				catch( UnsupportedEncodingException e )
				{
					throw Throwables.propagate(e);
				}
				break;

			case PUT:
				req = createPUT(request.getUrl(), convParams);
				mimeType = request.getMimeType();
				if( mimeType == null )
				{
					mimeType = ContentType.APPLICATION_JSON.getMimeType();
				}
				charset = request.getCharset();
				if( charset == null )
				{
					charset = ContentType.APPLICATION_JSON.getCharset().name();
				}
				try
				{
					((HttpPut) req).setEntity(new StringEntity(request.getBody(), mimeType, charset));
				}
				catch( UnsupportedEncodingException e )
				{
					throw Throwables.propagate(e);
				}
				break;

			case DELETE:
				req = createDELETE(request.getUrl(), convParams);
				break;

			case OPTIONS:
				req = createOPTIONS(request.getUrl(), convParams);
				break;

			default:
				req = null;
				break;
		}
		if( req != null )
		{
			for( Header header : convHeaders )
			{
				req.addHeader(header);
			}
		}
		return req;
	}

	public HttpGet createGET(String url, List<NameValuePair> params)
	{
		return new HttpGet(appendQueryString(url, queryStringNv(params)));
	}

	public HttpPut createPUT(String url, List<NameValuePair> params)
	{
		return new HttpPut(appendQueryString(url, queryStringNv(params)));
	}

	public HttpPost createPOST(String url, List<NameValuePair> params)
	{
		return new HttpPost(appendQueryString(url, queryStringNv(params)));
	}

	public HttpDelete createDELETE(String url, List<NameValuePair> params)
	{
		return new HttpDelete(appendQueryString(url, queryStringNv(params)));
	}

	public HttpOptions createOPTIONS(String url, List<NameValuePair> params)
	{
		return new HttpOptions(appendQueryString(url, queryStringNv(params)));
	}

	@NonNullByDefault(false)
	public static class ResponseImpl implements Response
	{
		private final HttpRequestBase httpMethod;
		private final HttpResponse response;

		private int code;
		private String message;
		private String body;
		private List<NameValue> headers = null;
		private boolean consumed;
		private InputStream in;

		public ResponseImpl(int code, String message)
		{
			this(null, null);
			this.code = code;
			this.message = message;
		}

		public ResponseImpl(HttpResponse response, HttpRequestBase httpMethod)
		{
			this.httpMethod = httpMethod;
			this.response = response;
			if( response != null )
			{
				final StatusLine statusLine = response.getStatusLine();
				this.code = statusLine.getStatusCode();
				this.message = statusLine.getReasonPhrase();
			}
		}

		@Override
		public int getCode()
		{
			return code;
		}

		@Override
		public String getMessage()
		{
			return message;
		}

		@Override
		public boolean isOk()
		{
			return code >= 200 && code < 300;
		}

		@Override
		public String getBody()
		{
			if( body == null )
			{
				final ByteArrayOutputStream out = new ByteArrayOutputStream(4096);
				try (InputStream in = getInputStream())
				{
					ByteStreams.copy(in, out);
					// TODO: check headers to see if indeed UTF8...
					body = new String(out.toByteArray(), Constants.UTF8);
					if( LOGGER.isTraceEnabled() )
					{
						LOGGER.trace("Received\n" + body);
					}
				}
				catch( IOException u )
				{
					throw Throwables.propagate(u);
				}
			}
			return body;
		}

		@Override
		public List<NameValue> getHeaders()
		{
			if( headers == null )
			{
				headers = Lists.newArrayList();
				if( response != null )
				{
					for( Header header : response.getAllHeaders() )
					{
						headers.add(new NameValue(header.getName(), header.getValue()));
					}
				}
			}
			return headers;
		}

		@Override
		public String getHeader(String name)
		{
			List<NameValue> hdrs = getHeaders();
			if( hdrs != null )
			{
				for( NameValue hdr : hdrs )
				{
					if( hdr.getName().equalsIgnoreCase(name) )
					{
						return hdr.getValue();
					}
				}
			}
			return null;
		}

		@Override
		public InputStream getInputStream() throws IOException
		{
			if( in == null )
			{
				if( response != null )
				{
					consumed = true;
					in = response.getEntity().getContent();
				}
			}
			return in;
		}

		@Override
		public boolean isStreaming()
		{
			if( response != null )
			{
				return response.getEntity().isStreaming();
			}
			return true;
		}

		@Override
		public void copy(OutputStream out)
		{
			try (InputStream in = getInputStream(); OutputStream o = out)
			{
				if( in != null )
				{
					ByteStreams.copy(in, o);
				}
				consumed = true;
			}
			catch( IOException io )
			{
				throw Throwables.propagate(io);
			}
			finally
			{
				close();
			}
		}

		@Override
		public void close()
		{
			if( response != null && !consumed )
			{
				final HttpEntity entity = response.getEntity();
				if( entity != null )
				{
					try
					{
						EntityUtils.consume(entity);
					}
					catch( IOException io )
					{
						throw Throwables.propagate(io);
					}
				}
				consumed = true;
			}

			try
			{
				Closeables.close(in, true);
			}
			catch( IOException ex )
			{
				// Ignore
			}

			if( httpMethod != null )
			{
				httpMethod.releaseConnection();
			}
		}
	}

	@Override
	public String toUrl(Request request)
	{
		return appendQueryString(request.getUrl(), queryString(request.getParams()));
	}

	/**
	 * Appends a query string to a URL, detecting if the URL already contains a
	 * query string
	 * 
	 * @param url
	 * @param queryString
	 * @return
	 */
	@Override
	public String appendQueryString(String url, @Nullable String queryString)
	{
		return url
			+ (queryString == null || queryString.equals("") ? "" : (url.contains("?") ? '&' : '?') + queryString);
	}

	/**
	 * @param paramNameValues An even number of strings indicating the parameter
	 *            names and values i.e. "param1", "value1", "param2", "value2"
	 * @return
	 */
	@Override
	public String queryString(String... paramNameValues)
	{
		final List<NameValuePair> params = Lists.newArrayList();
		// if( paramNameValues != null )
		{
			if( paramNameValues.length % 2 != 0 )
			{
				throw new RuntimeException("Must supply an even number of paramNameValues");
			}
			for( int i = 0; i < paramNameValues.length; i += 2 )
			{
				params.add(new BasicNameValuePair(paramNameValues[i], paramNameValues[i + 1]));
			}
		}
		return queryStringNv(params);
	}

	/**
	 * Turns a list of NameValuePair into a query string. e.g
	 * param1=val1&param2=val2
	 * 
	 * @param params
	 * @return
	 */
	@Nullable
	@Override
	public String queryString(@Nullable List<NameValue> params)
	{
		if( params == null )
		{
			return null;
		}
		return queryStringNv(Lists.transform(params, new ParamConverter()));
	}

	@Nullable
	private String queryStringNv(@Nullable List<NameValuePair> params)
	{
		if( params == null )
		{
			return null;
		}
		return URLEncodedUtils.format(params, Constants.UTF8);
	}

	/**
	 * URL <em>path</em> encodes a value
	 * 
	 * @param value
	 * @return
	 */
	@Override
	public String urlPathEncode(String value)
	{
		try
		{
			// FIXME: URLEncoder doesn't path encode...
			String encodedUrl = URLEncoder.encode(value, Constants.UTF8);
			// Ensure forward slashes are still slashes
			encodedUrl = encodedUrl.replaceAll("%2F", "/");
			// Ensure that pluses are changed into the correct %20
			encodedUrl = encodedUrl.replaceAll("\\+", "%20");
			return encodedUrl;
		}
		catch( UnsupportedEncodingException e )
		{
			// Can't happen
			throw new RuntimeException(e);
		}
	}

	/**
	 * Encodes a URL parameter value
	 * 
	 * @param value
	 * @return
	 */
	@Override
	public String urlParamEncode(String value)
	{
		try
		{
			return URLEncoder.encode(value, Constants.UTF8);
		}
		catch( UnsupportedEncodingException e )
		{
			// Can't happen
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean isError(Response response)
	{
		final int code = response.getCode();
		if( (code >= HTTP_OK && code < HTTP_MULT_CHOICE) || code == HTTP_UNAUTHORIZED || code == HTTP_PAYMENT_REQUIRED )
		{
			return false;
		}
		return true;
	}

	private static final class ParamConverter implements Function<NameValue, NameValuePair>
	{
		@Override
		public NameValuePair apply(NameValue nv)
		{
			return new BasicNameValuePair(nv.getName(), nv.getValue());
		}
	}

	private static final class HeaderConverter implements Function<NameValue, Header>
	{
		@Override
		public Header apply(NameValue nv)
		{
			return new BasicHeader(nv.getName(), nv.getValue());
		}
	}
}
