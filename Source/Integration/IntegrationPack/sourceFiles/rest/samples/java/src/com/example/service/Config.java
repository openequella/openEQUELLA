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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.servlet.ServletContext;

/**
 * REST client configuration
 */
public class Config
{
	private static final Config instance = new Config();

	private long lastLoaded;

	private String home;
	private String clientId;
	private String equellaUrl;
	private String proxyHost;
	private int proxyPort;
	private String collectionUuid;
	private String nameXpath;
	private String descriptionXpath;

	private Config()
	{
	}

	/**
	 * Load the configuration file found at the root folder
	 * 
	 * @throws IOException
	 */
	public static void ensureLoaded(ServletContext context) throws Exception
	{
		instance.ensureLoadedPrivate(context);
	}

	private synchronized void ensureLoadedPrivate(ServletContext context) throws Exception
	{
		String root = context.getRealPath("/");
		if( !(root.endsWith("\\") || root.endsWith("/")) )
		{
			root += "/";
		}
		final String configPath = root + "WEB-INF/config.properties";
		final File f = new File(configPath);
		if( lastLoaded < f.lastModified() )
		{
			try (InputStream in = new FileInputStream(f))
			{
				final Properties p = new Properties();
				p.load(in);

				// OAuth login values
				String homeUrl = p.getProperty("home.url");
				ensureProperty("home.url", homeUrl);
				if( !homeUrl.endsWith("/") )
				{
					homeUrl = homeUrl + "/";
				}
				home = homeUrl;

				String eqUrl = p.getProperty("equella.url");
				ensureProperty("equella.url", eqUrl);
				if( !eqUrl.endsWith("/") )
				{
					eqUrl = eqUrl + "/";
				}
				equellaUrl = eqUrl;

				clientId = p.getProperty("client_id");
				ensureProperty("client_id", clientId);

				// Contribution values
				collectionUuid = p.getProperty("collection.uuid");
				nameXpath = p.getProperty("name.xpath");
				descriptionXpath = p.getProperty("description.xpath");

				// Proxy settings
				String host = p.getProperty("proxy.host");
				if( host != null && host.trim().length() > 0 )
				{
					proxyHost = host;
					final String proxyPortString = p.getProperty("proxy.port");
					if( proxyPortString != null && proxyPortString.trim().length() > 0 )
					{
						proxyPort = Integer.valueOf(proxyPortString);
					}
					else
					{
						proxyPort = 80;
					}
				}
				else
				{
					proxyHost = null;
					proxyPort = 0;
				}

				lastLoaded = f.lastModified();
			}
		}
	}

	private void ensureProperty(String name, String value) throws Error
	{
		if( value == null )
		{
			throw new Error(name + " not set in WEB-INF/config.properties");
		}
	}

	/**
	 * home.url
	 * 
	 * @return The root URL of this application
	 */
	public static String getHome()
	{
		return instance.home;
	}

	/**
	 * client_id
	 * 
	 * @return The OAuth client_id of this application
	 */
	public static String getClientId()
	{
		return instance.clientId;
	}

	/**
	 * equella.url
	 * 
	 * @return The institution URL on the EQUELLA server
	 */
	public static String getEquellaUrl()
	{
		return instance.equellaUrl;
	}

	/**
	 * collection.uuid
	 * 
	 * @return The UUID of the collection to contribute new resources to
	 */
	public static String getCollectionUuid()
	{
		return instance.collectionUuid;
	}

	/**
	 * name.xpath
	 * 
	 * @return The XML path to the resource name for the collection defined in
	 *         collection.uuid
	 */
	public static String getNameXpath()
	{
		return instance.nameXpath;
	}

	/**
	 * description.xpath
	 * 
	 * @return The XML path to the resource description for the collection
	 *         defined in collection.uuid
	 */
	public static String getDescriptionXpath()
	{
		return instance.descriptionXpath;
	}

	/**
	 * proxy.host
	 * 
	 * @return A hostname for any proxy required to make internet connections
	 */
	public static String getProxyHost()
	{
		return instance.proxyHost;
	}

	/**
	 * proxy.port
	 * 
	 * @return The port for the proxy defined in proxy.host (if any)
	 */
	public static int getProxyPort()
	{
		return instance.proxyPort;
	}
}
