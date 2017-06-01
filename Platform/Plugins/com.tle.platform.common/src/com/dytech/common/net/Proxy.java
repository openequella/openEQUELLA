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

package com.dytech.common.net;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.Collection;
import java.util.Properties;

/**
 * Takes care of any proxy server related nonsense.
 * 
 * @author Nicholas Read
 */
public final class Proxy
{
	private Proxy()
	{
		throw new IllegalAccessError("Do not invoke");
	}

	public static void disableProxy()
	{
		setProxy(null, 0);
	}

	public static void setProxy(String proxyHost, int proxyPort)
	{
		setProxy(proxyHost, proxyPort, null, null);
	}

	public static void setProxy(String proxyHost, int proxyPort, String proxyUser, String proxyPassword)
	{
		setProxy(proxyHost, proxyPort, (String) null, proxyUser, proxyPassword);
	}

	public static void setProxy(String proxyHost, int proxyPort, Collection<String> exceptions, final String proxyUser,
		final String proxyPassword)
	{
		String nonProxyHosts = null;
		if( exceptions != null && !exceptions.isEmpty() )
		{
			StringBuilder exs = new StringBuilder();
			for( String ex : exceptions )
			{
				if( exs.length() > 0 )
				{
					exs.append('|');
				}
				exs.append(ex);
			}
			nonProxyHosts = exs.toString();
		}

		setProxy(proxyHost, proxyPort, nonProxyHosts, proxyUser, proxyPassword);
	}

	public static void setProxy(String proxyHost, int proxyPort, final String exceptions, final String proxyUser,
		final String proxyPassword)
	{
		Properties sysprops = System.getProperties();
		if( proxyHost != null && proxyHost.length() > 0 )
		{
			String proxyPortString = Integer.toString(proxyPort);
			// Older JVMs
			sysprops.put("proxySet", "true");
			sysprops.put("proxyHost", proxyHost);
			sysprops.put("proxyPort", proxyPortString);

			// Java 1.4 and up
			sysprops.put("http.proxyHost", proxyHost);
			sysprops.put("http.proxyPort", proxyPortString);
			sysprops.put("https.proxyHost", proxyHost);
			sysprops.put("https.proxyPort", proxyPortString);
			sysprops.put("ftp.proxyHost", proxyHost);
			sysprops.put("ftp.proxyPort", proxyPortString);

			// Setup any proxy host exceptions
			if( exceptions != null && exceptions.length() > 0 )
			{
				sysprops.put("http.nonProxyHosts", exceptions);
				sysprops.put("https.nonProxyHosts", exceptions);
				sysprops.put("ftp.nonProxyHosts", exceptions);
			}

			// Setup any authentication
			if( proxyUser != null && proxyUser.length() > 0 )
			{
				// JDK 1.4
				Authenticator.setDefault(new Authenticator()
				{
					@Override
					protected PasswordAuthentication getPasswordAuthentication()
					{
						return new PasswordAuthentication(proxyUser, proxyPassword.toCharArray());
					}
				});

				// Older JVMs
				sysprops.put("http.proxyUser", proxyUser);
				sysprops.put("http.proxyPassword", proxyPassword);
			}
		}
		else
		{
			sysprops.put("proxySet", "false");
			sysprops.put("http.proxySet", "false");
			sysprops.put("https.proxySet", "false");
			sysprops.put("ftp.proxySet", "false");
			sysprops.remove("proxyHost");
			sysprops.remove("http.proxyHost");
			sysprops.remove("https.proxyHost");
			sysprops.remove("ftp.proxyHost");
		}
		System.setProperties(sysprops);
	}
}