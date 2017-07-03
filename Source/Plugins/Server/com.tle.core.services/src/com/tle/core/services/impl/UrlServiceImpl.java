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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Enumeration;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.google.inject.name.Named;
import com.tle.beans.Institution;
import com.tle.common.Check;
import com.tle.common.institution.CurrentInstitution;
import com.tle.core.guice.Bind;
import com.tle.core.services.UrlService;

import hurl.build.QueryBuilder;
import hurl.build.UriBuilder;

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
			throw malformedUrl(ex, url, file);
		}
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
	public URI getBaseUriFromRequest(HttpServletRequest request)
	{
		Institution institution = CurrentInstitution.get();
		URI uri = (institution == null ? URI.create(getAdminUrl().toString()) : institution.getUrlAsUri());
		UriBuilder builder = UriBuilder.create(uri);
		builder.setScheme(request.isSecure() ? "https" : "http");
		return builder.build();
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

	private RuntimeException malformedUrl(Throwable ex, Object... bits)
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
}
