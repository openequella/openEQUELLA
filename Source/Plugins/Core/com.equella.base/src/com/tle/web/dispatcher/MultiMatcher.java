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

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.java.plugin.registry.Extension;

public class MultiMatcher
{
	private Map<String, Extension> extensions = new HashMap<String, Extension>();
	private Map<String, Extension> paths;
	private Map<String, Extension> exact = new HashMap<String, Extension>();

	public MultiMatcher()
	{
		paths = new TreeMap<String, Extension>(new Comparator<String>()
		{
			@Override
			public int compare(String o1, String o2)
			{
				int lenDif = o2.length() - o1.length();
				if( lenDif == 0 )
				{
					return o2.compareTo(o1);
				}
				return lenDif;
			}

		});
	}

	public void addMatcher(WebPathMatcher matcher, Extension ext)
	{
		for( String extension : matcher.getExtensions() )
		{
			extensions.put(extension, ext);
		}
		for( String path : matcher.getPaths() )
		{
			paths.put(path, ext);
		}
		for( String exactPath : matcher.getExact() )
		{
			exact.put(exactPath, ext);
		}
	}

	public ResolvedServlet matchPath(String path)
	{
		Extension ext = exact.get(path);
		if( ext != null )
		{
			return new ResolvedServlet(ext, path, null);
		}

		Iterator<String> iter = paths.keySet().iterator();
		while( iter.hasNext() )
		{
			String prefixPath = iter.next();
			if( path.startsWith(prefixPath) )
			{
				int len = prefixPath.length() - 1;
				return new ResolvedServlet(paths.get(prefixPath), prefixPath.substring(0, len), path.substring(len));
			}
		}
		for( Entry<String, Extension> extPath : extensions.entrySet() )
		{
			if( path.endsWith(extPath.getKey()) )
			{
				return new ResolvedServlet(extPath.getValue(), path, null);
			}
		}
		return null;
	}

	public static class ResolvedServlet
	{
		private final String servletPath;
		private final String pathInfo;
		private final Extension extension;

		public ResolvedServlet(Extension ext, String servletPath, String pathInfo)
		{
			this.extension = ext;
			this.servletPath = servletPath;
			this.pathInfo = pathInfo;
		}

		public String getServletPath()
		{
			return servletPath;
		}

		public String getPathInfo()
		{
			return pathInfo;
		}

		public Extension getExtension()
		{
			return extension;
		}
	}
}