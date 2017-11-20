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

package com.tle.web.dispatcher;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class WebPathMatcher
{
	private static Log LOGGER = LogFactory.getLog(WebPathMatcher.class);

	private final Set<String> extensions = new HashSet<String>();
	private final Set<String> paths;
	private final Set<String> exclusions;
	private final Set<String[]> exclusionPatterns;
	private final Set<String> exact = new HashSet<String>();
	private boolean all;

	public WebPathMatcher()
	{
		paths = new HashSet<String>();
		exclusions = new HashSet<String>();
		exclusionPatterns = new HashSet<String[]>();
	}

	@SuppressWarnings("nls")
	public void addPath(String path)
	{
		if( path.indexOf('*') == -1 )
		{
			exact.add(path);
		}
		else if( path.startsWith("/") && path.endsWith("/*") )
		{
			paths.add(path.substring(0, path.length() - 1));
			if( path.equals("/*") )
			{
				all = true;
			}
		}
		else if( path.startsWith("*.") )
		{
			extensions.add(path.substring(1));
		}
		else
		{
			LOGGER.error("Invalid mapping of '" + path + "'");
		}
	}

	public void addExclusion(String exclusion)
	{
		if( exclusion.indexOf('[') != -1 && exclusion.indexOf(']') != -1 )
		{
			exclusionPatterns.add(exclusion.split("/"));
		}
		else
		{
			exclusions.add(exclusion);
		}
	}

	public static Log getLOGGER()
	{
		return LOGGER;
	}

	public Set<String> getExtensions()
	{
		return extensions;
	}

	public Set<String> getPaths()
	{
		return paths;
	}

	public Set<String> getExact()
	{
		return exact;
	}

	public boolean isAll()
	{
		return all;
	}

	public boolean matches(String path)
	{
		return positiveMatches(path) && !excludeMatches(path);
	}

	private boolean positiveMatches(String path)
	{
		if( all )
		{
			return true;
		}

		if( exact.contains(path) )
		{
			return true;
		}

		Iterator<String> iter = paths.iterator();
		while( iter.hasNext() )
		{
			if( path.startsWith(iter.next()) )
			{
				return true;
			}
		}
		for( String ext : extensions )
		{
			if( path.endsWith(ext) )
			{
				return true;
			}
		}
		return false;
	}

	@SuppressWarnings("nls")
	private boolean excludeMatches(String path)
	{
		if( exclusions.contains(path) )
		{
			return true;
		}

		String[] splitPath = null;

		for( String[] splitPattern : exclusionPatterns )
		{
			if( splitPath == null )
			{
				splitPath = path.split("/");
			}

			if( splitPath.length != splitPattern.length )
			{
				continue;
			}

			for( int i = 0; i < splitPath.length; i++ )
			{
				if( !(splitPattern[i].startsWith("[") && splitPattern[i].endsWith("]"))

				&& !splitPath[i].equals(splitPattern[i]) )
				{
					break;
				}
				if( splitPattern[i].equals("*") )
				{
					return true;
				}
				if( i == splitPath.length - 1 )
				{
					return true;
				}
			}
		}
		return false;
	}
}
