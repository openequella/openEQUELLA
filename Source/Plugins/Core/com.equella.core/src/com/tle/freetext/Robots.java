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

package com.tle.freetext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

/**
 * A small class to parse the robots.txt file that resides on servers to
 * indicate which paths can be accessed on a server. Eg.
 * http://www.google.com/robots.txt <code>
 * User-agent:
 * Disallow: /search
 * Disallow: /groups
 * Disallow: /images
 * Disallow: /catalogs
 * Disallow: /catalog_list
 * ...
 * </code> This indicates that any paths beginning with the above paths are NOT
 * allowed to be searched by robots (such as our FreeText indexer). As that is
 * required by this class is to call the @see #isAllowed(String) method which
 * will return true or false for a given path residing on the server is allowed
 * to be indexed.
 * 
 * @author cofarrell
 */
public class Robots
{
	private static final Logger LOGGER = Logger.getLogger(Robots.class);
	private URL robot;
	private Set<String> disallowedPaths;
	private Set<String> allowedPaths;

	/**
	 * See @see #Robots(URL).
	 * 
	 * @param url
	 * @throws Exception
	 */
	public Robots(String url) throws Exception
	{
		URL fullUrl = new URL(url);
		setup(fullUrl);
	}

	/**
	 * Parses the robot text file for the site of a given url.
	 * 
	 * @param url
	 * @throws MalformedURLException
	 */
	public Robots(URL url) throws MalformedURLException
	{
		setup(url);
	}

	protected void setup(URL url) throws MalformedURLException
	{
		// bizarely, a null host does not throw a malformed
		if( url.getHost() == null )
		{
			throw new MalformedURLException("Url has no host: " + url);
		}
		robot = new URL(url.getProtocol(), url.getHost(), url.getPort(), "/robots.txt");
		getRobotPaths();
	}

	/**
	 * See @see #isAllowed(String).
	 * 
	 * @param url
	 * @return
	 */
	public boolean isAllowed(URL url)
	{
		return isAllowed(url.getPath());
	}

	/**
	 * Determines from the Disallow and Allow properties of a robots file
	 * whether a given path is allowed to be searched by a robot.
	 * 
	 * @param path
	 * @return true if given path can be searched
	 */
	public boolean isAllowed(String path)
	{
		Iterator<String> i = disallowedPaths.iterator();
		boolean isAllowed = true;
		while( isAllowed && i.hasNext() )
		{
			String disallowed = i.next();
			if( path.startsWith(disallowed) )
			{
				isAllowed = false;
			}
		}

		if( !isAllowed )
		{
			i = allowedPaths.iterator();
			while( !isAllowed && i.hasNext() )
			{
				String allowed = i.next();
				if( path.startsWith(allowed) )
				{
					isAllowed = true;
				}
			}
		}

		return isAllowed;
	}

	@SuppressWarnings("nls")
	protected void getRobotPaths()
	{
		if( disallowedPaths != null )
		{
			return;
		}

		disallowedPaths = new HashSet<String>();
		allowedPaths = new HashSet<String>();

		try
		{
			URLConnection connection = robot.openConnection();
			connection.setConnectTimeout((int) TimeUnit.MINUTES.toMillis(1));
			connection.setReadTimeout((int) TimeUnit.MINUTES.toMillis(1));

			try( BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream())) )
			{
				String line = reader.readLine();
				while( line != null && disallowedPaths.size() == 0 )
				{
					if( line.indexOf("User-agent:") >= 0 && line.indexOf('*') >= 0 )
					{
						line = reader.readLine();
						while( line != null && line.indexOf("User-agent:") < 0 )
						{

							int comment = line.indexOf('#');
							if( comment >= 0 )
							{
								line = line.substring(0, comment);
							}

							// expecting colon plus space plus value, eg
							// "Allow: meaningful"
							// but avoid lines which simply read "Allow:" (ie,
							// nothing after colon)
							int collon = line.indexOf(':');
							if( collon >= 0 && line.length() >= collon + 2 )
							{

								if( line.indexOf("Disallow:") >= 0 )
								{
									String disallow = line.substring(collon + 2);
									disallowedPaths.add(disallow);
								}

								else if( line.indexOf("Allow:") >= 0 )
								{
									String allow = line.substring(collon + 2);
									allowedPaths.add(allow);
								}
							}

							line = reader.readLine();
						}
					}
					line = reader.readLine();
				}
			}
		}
		catch( IOException ie )
		{
			LOGGER.info(robot + " does not exist");
		}
	}
}
