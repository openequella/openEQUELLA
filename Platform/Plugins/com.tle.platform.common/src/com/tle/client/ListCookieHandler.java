/*
 * Copyright 2019 Apereo
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

package com.tle.client;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.URI;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

import com.tle.common.Check;

@SuppressWarnings("nls")
public class ListCookieHandler extends CookieHandler
{
	private static final Logger LOGGER = Logger.getLogger(ListCookieHandler.class.getName());

	private List<Cookie> cache = new LinkedList<Cookie>();
	private boolean ignoreCookieOverrideAttempts;

	public void setIgnoreCookieOverrideAttempts(boolean ignoreCookieOverrideAttempts)
	{
		this.ignoreCookieOverrideAttempts = ignoreCookieOverrideAttempts;
	}

	public void clearCookies()
	{
		cache.clear();
	}

	public List<String> splitCookieString(String cookie)
	{
		if( Check.isEmpty(cookie) )
		{
			return Collections.emptyList();
		}

		final String[] cookies = cookie.split(";");
		for( int i = 0, count = cookies.length; i < count; i++ )
		{
			cookies[i] = cookies[i].trim();
		}
		return Arrays.asList(cookies);
	}

	@Override
	public void put(URI uri, Map<String, List<String>> responseHeaders) throws IOException
	{
		List<String> setCookieList = responseHeaders.get("Set-Cookie");
		if( setCookieList != null )
		{
			for( String item : setCookieList )
			{
				put(new Cookie(uri, item));
			}
		}
	}

	private void put(Cookie cookie)
	{
		for( Cookie existingCookie : cache )
		{
			if( (cookie.getURI().equals(existingCookie.getURI()))
				&& (cookie.getName().equals(existingCookie.getName())) )
			{
				if( ignoreCookieOverrideAttempts )
				{
					LOGGER.info("Ignoring attempt to change cookie " + cookie.getName() + " from "
						+ existingCookie.getValue() + " to " + cookie.getValue());
				}
				else
				{
					LOGGER.info("Changing cookie " + cookie.getName() + " from " + existingCookie.getValue() + " to "
						+ cookie.getValue());
					cache.remove(existingCookie);
					cache.add(cookie);
				}
				return;
			}
		}
		LOGGER.info("Adding cookie " + cookie.getName() + " with value " + cookie.getValue());
		cache.add(cookie);
	}

	@Override
	public Map<String, List<String>> get(URI uri, Map<String, List<String>> requestHeaders) throws IOException
	{
		// Retrieve all the cookies for matching URI
		// Put in comma-separated list
		StringBuilder cookies = new StringBuilder();
		for( Cookie cookie : cache )
		{
			// Remove cookies that have expired
			if( cookie.hasExpired() )
			{
				cache.remove(cookie);
			}
			else if( cookie.matches(uri) )
			{
				if( cookies.length() > 0 )
				{
					cookies.append(", ");
				}
				cookies.append(cookie.toString());
			}
		}

		// Map to return
		Map<String, List<String>> cookieMap = new HashMap<String, List<String>>(requestHeaders);

		// Convert StringBuilder to List, store in map
		if( cookies.length() > 0 )
		{
			List<String> list = Collections.singletonList(cookies.toString());
			cookieMap.put("Cookie", list);
		}
		return Collections.unmodifiableMap(cookieMap);
	}
}

@SuppressWarnings("nls")
class Cookie
{
	private DateFormat expiresFormat1 = new SimpleDateFormat("E, dd MMM yyyy k:m:s 'GMT'", Locale.US);
	private DateFormat expiresFormat2 = new SimpleDateFormat("E, dd-MMM-yyyy k:m:s 'GMT'", Locale.US);

	private String name;
	private String value;
	private URI uri;
	private Date expires;
	private String path;

	public Cookie(URI uri, String header)
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
			String name = nameValue.substring(0, equals);
			String value = nameValue.substring(equals + 1);
			if( name.equalsIgnoreCase("domain") )
			{
				String uriDomain = uri.getHost();
				if( !uriDomain.equals(value) )
				{
					if( !value.startsWith(".") )
					{
						value = "." + value;
					}
					uriDomain = uriDomain.substring(uriDomain.indexOf('.'));
					if( !uriDomain.equals(value) )
					{
						throw new IllegalArgumentException("Trying to set foreign cookie");
					}
				}
			}
			else if( name.equalsIgnoreCase("path") )
			{
				this.path = value;
			}
			else if( name.equalsIgnoreCase("expires") )
			{
				try
				{
					this.expires = expiresFormat1.parse(value);
				}
				catch( ParseException e )
				{
					try
					{
						this.expires = expiresFormat2.parse(value);
					}
					catch( ParseException e2 )
					{
						throw new IllegalArgumentException("Bad date format in header: " + value);
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

		String path = uri.getPath();
		if( path == null )
		{
			path = "/";
		}

		return path.startsWith(this.path);
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
