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

package com.tle.common;

import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class URLUtils
{
	public static final String CHARSET_ENCODING = "UTF-8"; //$NON-NLS-1$
	private static final Pattern URL_ENCODE_ANCHORS = Pattern.compile("^(.*)%23([^/]*?)$"); //$NON-NLS-1$

	public static URL newURL(String url)
	{
		try
		{
			return new URL(url);
		}
		catch( MalformedURLException mal )
		{
			throw new RuntimeException(url, mal);
		}
	}

	public static URL newURL(String context, String spec)
	{
		return newURL(newURL(context), spec);
	}

	@SuppressWarnings("nls")
	public static URL newURL(URL context, String spec)
	{
		try
		{
			String ctx = context.toString();
			if( !ctx.endsWith("/") )
			{
				ctx += "/";
			}
			return new URL(newURL(ctx), spec);
		}
		catch( MalformedURLException mal )
		{
			throw new RuntimeException(context.toString() + ' ' + spec, mal);
		}
	}

	public static String basicUrlEncode(String url)
	{
		try
		{
			return URLEncoder.encode(url, CHARSET_ENCODING);
		}
		catch( UnsupportedEncodingException e )
		{
			throw new RuntimeException(e);
		}
	}

	public static String basicUrlDecode(String url)
	{
		try
		{
			return URLDecoder.decode(url, CHARSET_ENCODING);
		}
		catch( UnsupportedEncodingException e )
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * Synonymous with basicUrlPathEncode
	 */
	public static String urlEncode(String url)
	{
		return basicUrlPathEncode(url, true);
	}

	public static String urlEncode(String url, boolean preserveAnchor)
	{
		return basicUrlPathEncode(url, preserveAnchor);
	}

	private static String basicUrlPathEncode(String url, boolean preserveAnchor)
	{
		String encodedUrl = basicUrlEncode(url);

		// Ensure forward slashes are still slashes
		encodedUrl = encodedUrl.replaceAll("%2F", "/"); //$NON-NLS-1$ //$NON-NLS-2$

		// Ensure that pluses are changed into the correct %20
		encodedUrl = encodedUrl.replaceAll("\\+", "%20"); //$NON-NLS-1$ //$NON-NLS-2$

		if( preserveAnchor )
		{
			// Ensure that the last # after any forward slash is reestablished,
			// as
			// it is the URL anchor
			Matcher m = URL_ENCODE_ANCHORS.matcher(encodedUrl);
			if( m.matches() )
			{
				encodedUrl = m.group(1) + '#' + m.group(2);
			}
		}

		return encodedUrl;
	}

	/**
	 * @param url
	 * @return true if the url is of the form http://dddsdasd etc.
	 */
	@SuppressWarnings("unused")
	public static boolean isAbsoluteUrl(String url)
	{
		try
		{
			URI uri = new URI(url);
			return uri.getAuthority() != null;
		}
		catch( URISyntaxException mal )
		{
			return false;
		}
	}

	@SuppressWarnings("nls")
	public static Map<String, Set<String>> parseQueryString(String queryString)
	{
		final Map<String, Set<String>> qs = new HashMap<String, Set<String>>();
		if( queryString != null )
		{
			final String[] params = queryString.split("&");
			for( String param : params )
			{
				final String[] nameVal = param.split("=");
				if( nameVal.length == 2 )
				{
					final String key = nameVal[0];
					Set<String> current = qs.get(key);
					if( current == null )
					{
						current = new HashSet<String>();
						qs.put(key, current);
					}
					current.add(nameVal[1]);
				}
			}
		}
		return qs;
	}

	/**
	 * More convenient to extract the values out.
	 * 
	 * @param queryString
	 * @param singleValues
	 * @return
	 */
	@SuppressWarnings("nls")
	public static Map<String, String> parseQueryString(String queryString, boolean singleValues)
	{
		final Map<String, String> qs = new HashMap<String, String>();
		if( queryString != null )
		{
			if( queryString.startsWith("?") )
			{
				queryString = queryString.substring(1);
			}
			final String[] params = queryString.split("&");
			for( String param : params )
			{
				final String[] nameVal = param.split("=");
				if( nameVal.length == 2 )
				{
					qs.put(nameVal[0], nameVal[1]);
				}
			}
		}
		return qs;
	}

	public static String appendQueryString(String url, String qs)
	{
		if( url.indexOf('?') > 0 )
		{
			return url + '&' + qs;
		}
		return url + '?' + qs;
	}

	/**
	 * @param map One of: Map<?,Object[]> Map<?,Collection<Object>>
	 *            Map<?,Object>
	 * @return Query string without the "?" prefix
	 */
	public static String getParameterString(Map<?, ?> map)
	{
		List<NameValue> nvs = new ArrayList<NameValue>();
		for( Map.Entry<?, ?> entry : map.entrySet() )
		{
			Collection<?> values = Collections.emptyList();

			Object obj = entry.getValue();
			if( obj != null )
			{
				if( obj instanceof Object[] )
				{
					values = Arrays.asList((Object[]) obj);
				}
				else if( obj instanceof Collection<?> )
				{
					values = Collection.class.cast(obj);
				}
				else
				{
					values = Collections.singleton(obj);
				}
			}

			for( Object name2 : values )
			{
				String element = name2.toString();
				nvs.add(new NameValue(entry.getKey().toString(), element));
			}
		}

		StringBuilder parameters = new StringBuilder();

		boolean first = true;
		for( NameValue nv : nvs )
		{
			if( !first )
			{
				parameters.append('&');
			}
			else
			{
				first = false;
			}

			parameters.append(URLUtils.basicUrlEncode(nv.getName()));
			parameters.append('=');
			parameters.append(URLUtils.basicUrlEncode(nv.getValue()));
		}

		return parameters.toString();
	}

	/**
	 * Extracts into 2 parts, the base URL and the query string.  The returned query string won't be null (ie it is blank if empty), and does not contain the '?' character
	 * @param url
	 * @return
	 */
	public static String[] decompose(String url)
	{
		String[] bits = new String[2];
		int questionIndex = url.indexOf("?");
		if( questionIndex >= 0 )
		{
			bits[0] = Utils.safeSubstring(url, 0, questionIndex);
			bits[1] = Utils.safeSubstring(url, questionIndex + 1);
		}
		else
		{
			bits[0] = url;
			bits[1] = "";
		}
		return bits;
	}
}
