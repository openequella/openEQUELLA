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

package com.dytech.common.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;

/**
 * A basic http downloader, one day set to become as powerful as Wget and Curl!
 * 
 * @author Nicholas Read
 */
public class Wget
{
	private WgetConnectionHandler handler;
	private final Map<String, String> requestParams;
	private final Map<String, List<String>> headers;
	private boolean useCookies;
	private String cookie;

	public Wget()
	{
		super();
		requestParams = new HashMap<String, String>();
		headers = new HashMap<String, List<String>>();
		useCookies = true;
	}

	public Wget(WgetConnectionHandler handler)
	{
		this();
		setHandler(handler);
	}

	public void setCookies(boolean useCookies)
	{
		this.useCookies = useCookies;
	}

	public void setRequestParameter(String key, String value) throws UnsupportedEncodingException
	{
		requestParams.put(encode(key), encode(value));
	}

	public void removeRequestParameter(String key) throws UnsupportedEncodingException
	{
		requestParams.remove(encode(key));
	}

	public void clearRequestParameters()
	{
		requestParams.clear();
	}

	public void setHeader(String key, String value)
	{
		List<String> vals = headers.get(key);
		if( vals == null )
		{
			vals = new ArrayList<String>();
			headers.put(key, vals);
		}
		vals.add(value);
	}

	public void removeHeader(String key)
	{
		headers.remove(key);
	}

	public void clearHeaders()
	{
		headers.clear();
	}

	private String encode(String s) throws UnsupportedEncodingException
	{
		return URLEncoder.encode(s, "UTF-8");
	}

	/**
	 * The following method is a cut-n-paste rehashing of the ANT Get task which
	 * is licenced under the Apache licence.
	 */
	public void retrieveURL(URL source, OutputStream target) throws IOException
	{
		InputStream input = null;
		try
		{
			URLConnection connection = source.openConnection();

			if( useCookies && cookie != null )
			{
				connection.setRequestProperty("Cookie", cookie);
			}

			for( Map.Entry<String, List<String>> header : headers.entrySet() )
			{
				String headerName = header.getKey();
				List<String> values = header.getValue();
				StringBuilder valuesString = new StringBuilder();
				boolean first = true;
				for( String val : values )
				{
					if( !first )
					{
						valuesString.append(",");
					}
					valuesString.append(val);
					first = false;
				}
				connection.setRequestProperty(headerName, valuesString.toString());
			}

			if( !requestParams.isEmpty() )
			{
				// We will POST the parameters to ensure the URL is the same.
				connection.setDoOutput(true);

				StringBuilder parameters = new StringBuilder();
				for( String key : requestParams.keySet() )
				{
					String value = requestParams.get(key);

					if( parameters.length() > 0 )
					{
						parameters.append('&');
					}
					parameters.append(key);
					parameters.append('=');
					parameters.append(value);
				}

				byte[] paramBytes = parameters.toString().getBytes();
				String paramLength = Integer.toString(paramBytes.length);

				connection.setRequestProperty("Content-Length", paramLength);
				OutputStream oStream = connection.getOutputStream();
				oStream.write(paramBytes);
				oStream.flush();
				oStream.close();
			}
			connection.connect();

			if( useCookies )
			{
				String setcookie = connection.getHeaderField("Set-Cookie");
				if( setcookie != null )
				{
					int index = setcookie.indexOf(';');
					if( index >= 0 )
					{
						setcookie = setcookie.substring(0, index);
					}
					if( setcookie != null && setcookie.length() > 0 )
					{
						cookie = setcookie;
					}
				}
			}

			if( handler != null )
			{
				handler.connectionMade(connection);
			}

			// Retry three times
			boolean ok = false;
			IOException lastException = null;
			for( int i = 0; i < 3 && !ok; i++ )
			{
				try
				{
					input = connection.getInputStream();
					ok = true;
				}
				catch( IOException ex )
				{
					// This might be expected. Just try again.
					lastException = ex;
				}
			}

			if( input == null )
			{
				throw new IOException("Could not open connection", lastException);
			}

			ByteStreams.copy(input, target);
		}
		finally
		{
			Closeables.close(input, true);
		}
	}

	public WgetConnectionHandler getHandler()
	{
		return handler;
	}

	public final void setHandler(WgetConnectionHandler handler)
	{
		this.handler = handler;
	}

	public static void getURL(URL source, OutputStream target) throws IOException
	{
		new Wget().retrieveURL(source, target);
	}
}