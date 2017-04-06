package com.tle.core.services.impl;

import hurl.build.QueryBuilder;
import hurl.build.UriBuilder;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.google.inject.name.Named;
import com.tle.beans.Institution;
import com.tle.common.Check;
import com.tle.core.guice.Bind;
import com.tle.core.services.UrlService;
import com.tle.core.user.CurrentInstitution;

/**
 * @author Nicholas Read
 */
@SuppressWarnings("nls")
@Bind(UrlService.class)
@Singleton
public class UrlServiceImpl implements UrlService
{
	private static final Logger LOGGER = Logger.getLogger(UrlServiceImpl.class);

	public static final String PREVIEW = "preview/";
	public static final String ITEMS = "items/";
	public static final String INTEG = "integ/";

	private final URL adminUrl;

	@Inject
	public UrlServiceImpl(@Named("admin.url") URL adminUrl)
	{
		this.adminUrl = append(adminUrl, "");
		LOGGER.info("Admin URL is " + adminUrl.toString());
	}

	@Override
	public URL getAdminUrl()
	{
		return adminUrl;
	}

	@Override
	public URL getInstitutionUrl()
	{
		return getInstitutionUrl(CurrentInstitution.get());
	}

	/**
	 * Assumes given URL ends with a forward slash, and always returns a URL
	 * ending with a forward slash.
	 */
	private URL append(URL url, String extra)
	{
		String file = url.getFile() + extra;
		if( !file.endsWith("/") )
		{
			file += '/';
		}

		try
		{
			return new URL(url, file);
		}
		catch( MalformedURLException ex )
		{
			throw malformed(ex, url, file);
		}
	}

	@Override
	public URL getInstitutionUrl(Institution institution)
	{
		if( institution == null )
		{
			return adminUrl;
		}
		return institution.getUrlAsUrl();
	}

	@Override
	public String institutionalise(String url)
	{
		String result;
		try
		{
			result = new URL(getInstitutionUrl(), url).toString();
		}
		catch( MalformedURLException e )
		{
			throw malformed(e, url);
		}
		return result;
	}

	@Override
	public QueryBuilder getQueryBuilderForRequest(HttpServletRequest request)
	{
		QueryBuilder qbuilder = QueryBuilder.create();
		Enumeration<String> paramEnum = request.getParameterNames();
		while( paramEnum.hasMoreElements() )
		{
			String paramName = paramEnum.nextElement();
			String[] vals = request.getParameterValues(paramName);
			if( vals != null && vals.length > 0 )
			{
				for( String val : vals )
				{
					qbuilder.addParam(paramName, val);
				}
			}
			else
			{
				qbuilder.addParam(paramName);
			}
		}
		return qbuilder;
	}

	@Override
	public URI getUriForRequest(HttpServletRequest request, String query)
	{
		Institution institution = CurrentInstitution.get();
		URI uri = institution.getUrlAsUri();
		UriBuilder builder = UriBuilder.create(uri);
		builder.setScheme(request.isSecure() ? "https" : "http");
		builder.setPath(request.getRequestURI());
		builder.setQuery(query);
		return builder.build();
	}

	@Override
	public URI getInstitutionUri()
	{
		try
		{
			return getInstitutionUrl().toURI();
		}
		catch( URISyntaxException e )
		{
			throw new IllegalArgumentException(e);
		}
	}

	@Override
	public URI getBaseUriFromRequest(HttpServletRequest request)
	{
		Institution institution = CurrentInstitution.get();
		URL institutionUrl = getInstitutionUrl(institution);
		try
		{
			UriBuilder builder = UriBuilder.create(institutionUrl.toURI());
			builder.setScheme(request.isSecure() ? "https" : "http");
			return builder.build();
		}
		catch( URISyntaxException e )
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean isInstitutionUrl(String url)
	{
		try
		{
			URL iUrl = getInstitutionUrl();
			URL myUrl = new URL(url);
			int myPort = myUrl.getPort();
			if( myPort == -1 )
			{
				myPort = myUrl.getDefaultPort();
			}
			int iPort = iUrl.getPort();
			if( iPort == -1 )
			{
				iPort = iUrl.getDefaultPort();
			}
			return (iUrl.getHost().equals(myUrl.getHost()) && (myPort == iPort) && myUrl.getPath().startsWith(
				iUrl.getPath()));
		}
		catch( MalformedURLException e )
		{
			return false;
		}
	}

	@Override
	public String removeInstitution(String url)
	{
		try
		{
			URL iUrl = getInstitutionUrl();
			URL myUrl = new URL(url);
			String myRef = myUrl.getRef(); // anchor e.g. #post1
			String myUrlNoHost = myUrl.getFile() + (myRef == null ? "" : "#" + myRef);
			return myUrlNoHost.substring(iUrl.getPath().length());
		}
		catch( MalformedURLException ex )
		{
			throw malformed(ex, url);
		}
	}

	protected RuntimeException malformed(Throwable ex, Object... bits)
	{
		StringBuilder msg = new StringBuilder("Error creating URL");
		for( Object bit : bits )
		{
			if( bit != null )
			{
				msg.append(", ");
				msg.append(bit.toString());
			}
		}

		return new RuntimeException(msg.toString(), ex);
	}

	@Override
	public boolean isRelativeUrl(String url)
	{
		try
		{
			return !Check.isEmpty(new URL(url).getHost());
		}
		catch( MalformedURLException mal )
		{
			return true;
		}
	}
}
