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

package com.tle.encoding;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

public final class UrlEncodedString
{
	public static final UrlEncodedString BLANK = new UrlEncodedString(""); //$NON-NLS-1$

	private static final String UTF_8 = "UTF-8"; //$NON-NLS-1$

	private final String encodedString;

	private UrlEncodedString(String url)
	{
		this.encodedString = url;
	}

	public static UrlEncodedString createFromValue(String value)
	{
		try
		{
			value = URLEncoder.encode(value, UTF_8);
		}
		catch( UnsupportedEncodingException e )
		{
			throw new RuntimeException(e);
		}
		return new UrlEncodedString(value);
	}

	public static UrlEncodedString createFromFilePath(String path)
	{
		try
		{
			path = URLEncoder.encode(path, UTF_8);
		}
		catch( UnsupportedEncodingException e )
		{
			throw new RuntimeException(e);
		}
		// Ensure forward slashes are still slashes
		path = path.replaceAll("%2F", "/"); //$NON-NLS-1$ //$NON-NLS-2$

		// Ensure that pluses are changed into the correct %20
		path = path.replaceAll("\\+", "%20"); //$NON-NLS-1$ //$NON-NLS-2$
		return new UrlEncodedString(path);
	}

	public String getUnencodedString()
	{
		try
		{
			return URLDecoder.decode(encodedString, UTF_8);
		}
		catch( UnsupportedEncodingException e )
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public String toString()
	{
		return encodedString;
	}
}
