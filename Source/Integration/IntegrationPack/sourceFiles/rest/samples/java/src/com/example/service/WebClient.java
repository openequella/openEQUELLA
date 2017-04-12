/*
 * Copyright (c) 2011, EQUELLA All rights reserved. Redistribution and use in
 * source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met: Redistributions of source code must
 * retain the above copyright notice, this list of conditions and the following
 * disclaimer. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution. Neither
 * the name of EQUELLA nor the names of its contributors may be used to endorse
 * or promote products derived from this software without specific prior written
 * permission. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package com.example.service;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.cert.CertificateException;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import com.example.util.UrlUtils;

/**
 * A service which can make HTTP requests to a server
 */
public class WebClient
{
	private static final WebClient instance = new WebClient();

	private final ClientConnectionManager conMan;
	private final SSLSocketFactory blindFactory;

	private WebClient()
	{
		try
		{
			conMan = new PoolingClientConnectionManager();

			// Allows us to just accept all SSL certs
			final TrustManager[] blindTrustMan = new TrustManager[]{new X509TrustManager()
			{
				@Override
				public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType)
					throws CertificateException
				{
				}

				@Override
				public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType)
					throws CertificateException
				{
				}

				@Override
				public java.security.cert.X509Certificate[] getAcceptedIssuers()
				{
					return new java.security.cert.X509Certificate[0];
				}
			}};

			final SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, blindTrustMan, new java.security.SecureRandom());
			blindFactory = sc.getSocketFactory();

			conMan.getSchemeRegistry().register(new Scheme("https", 443, new SchemeSocketFactory()
			{
				@Override
				public Socket connectSocket(Socket s, InetSocketAddress i, InetSocketAddress i2, HttpParams h)
					throws IOException, UnknownHostException, ConnectTimeoutException
				{
					s.connect(i, 60);
					return s;
				}

				@Override
				public Socket createSocket(HttpParams p) throws IOException
				{
					return blindFactory.createSocket();
				}

				@Override
				public boolean isSecure(Socket arg0) throws IllegalArgumentException
				{
					return false;
				}
			}));
		}
		catch( Exception e )
		{
			throw new RuntimeException(e);
		}
	}

	public static HttpGet createGET(String url, List<NameValuePair> params)
	{
		return new HttpGet(UrlUtils.appendQueryString(url, UrlUtils.queryString(params)));
	}

	public static HttpPut createPUT(String url, List<NameValuePair> params)
	{
		return new HttpPut(UrlUtils.appendQueryString(url, UrlUtils.queryString(params)));
	}

	public static HttpPost createPOST(String url, List<NameValuePair> params)
	{
		return new HttpPost(UrlUtils.appendQueryString(url, UrlUtils.queryString(params)));
	}

	/**
	 * @param request
	 * @param consume Eat the content of the response. You <em>must</em> set
	 *            this to true if you are not going to read the response
	 *            content.
	 * @param token
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static HttpResponse execute(final HttpUriRequest request, final boolean consume, final String token)
		throws ClientProtocolException, IOException
	{
		return instance.executePrivate(request, consume, token);
	}

	private HttpResponse executePrivate(final HttpUriRequest request, final boolean consume, final String token)
	{
		if( token != null )
		{
			final Header tokenHeader = new BasicHeader("X-Authorization", "access_token=" + token);
			request.setHeader(tokenHeader);
		}

		try
		{
			final HttpClient client = createClient(request.getURI().getScheme().equals("https"));
			if( Config.getProxyHost() != null )
			{
				final HttpHost proxy = new HttpHost(Config.getProxyHost(), Config.getProxyPort());
				client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
			}
			final HttpResponse response = client.execute(request);
			if( consume )
			{
				EntityUtils.consume(response.getEntity());
			}
			return response;
		}
		catch( Exception e )
		{
			throw new RuntimeException(e);
		}
	}

	private HttpClient createClient(boolean https)
	{
		if( !https )
		{
			return new DefaultHttpClient();
		}
		return new DefaultHttpClient(conMan);
	}
}
