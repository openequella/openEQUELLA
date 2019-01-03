/*
 * Licensed to the Apereo Foundation under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.tle.common;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("nls")
public abstract class PathUtils
{
	protected PathUtils()
	{
		throw new Error("No");
	}

	public static String urlPath(String... urlBits)
	{
		return filePath(urlBits);
	}

	public static String urlEncodedPath(String... pathBits)
	{
		return URLUtils.urlEncode(filePath(pathBits), false);
	}

	/**
	 * @param pathBits
	 * @return A file path built out of pathBits. E.g. bit/bit2/bit3
	 */
	public static String filePath(String... pathBits)
	{
		final List<String> nonEmptyBits = new ArrayList<String>();
		for( String bit : pathBits )
		{
			if( !Check.isEmpty(bit) )
			{
				nonEmptyBits.add(removeTrailingSlash(bit));
			}
		}
		if( nonEmptyBits.size() == 0 )
		{
			return "";
		}
		if( nonEmptyBits.size() == 1 )
		{
			return nonEmptyBits.get(0);
		}

		String bit0 = nonEmptyBits.get(0);
		final StringBuilder path = new StringBuilder(bit0);
		for( int i = 1; i < nonEmptyBits.size(); i++ )
		{
			String bit = nonEmptyBits.get(i);
			if( !Check.isEmpty(bit) )
			{
				if( !bit.startsWith("/") )
				{
					// We have to use forward slashes because some file paths
					// are used in URLs
					path.append('/');
				}
				path.append(bit);
			}
		}
		return path.toString();
	}

	/**
	 * Breaks filename into a path with no file extension, and the extension
	 * (with no dot)
	 * 
	 * @param filename
	 * @return
	 */
	public static Pair<String, String> fileParts(String filename)
	{
		String fullSrcPath = filename;
		String fileNoExt = fullSrcPath;
		String ext = "";
		int dot = fullSrcPath.lastIndexOf('.');
		if( dot > 0 )
		{
			fileNoExt = fullSrcPath.substring(0, dot);
			ext = fullSrcPath.substring(dot + 1);
		}
		return new Pair<String, String>(fileNoExt, ext);
	}

	public static String getFilenameFromFilepath(String filepath)
	{
		String fixedPath = removeTrailingSlash(filepath);

		int ind = fixedPath.lastIndexOf('/');
		if( ind == -1 )
		{
			return fixedPath;
		}
		return fixedPath.substring(ind + 1);
	}

	private static String removeTrailingSlash(String filepath)
	{
		String fixedPath = filepath.replaceAll("\\\\", "/");
		while( fixedPath.endsWith("/") )
		{
			fixedPath = fixedPath.substring(0, fixedPath.length() - 1);
		}
		return fixedPath;
	}

	public static String getParentFolderFromFilepath(String filepath)
	{
		String fixedPath = removeTrailingSlash(filepath);

		int ind = fixedPath.lastIndexOf('/');
		if( ind == -1 )
		{
			return null;
		}
		return fixedPath.substring(0, ind);
	}

	public static String fileencode(String szStr)
	{
		StringBuilder szOut = new StringBuilder();
		for( int i = 0; i < szStr.length(); i++ )
		{
			char ch = szStr.charAt(i);
			switch( ch )
			{
				case ':':
				case '*':
				case '?':
				case '"':
				case '<':
				case '>':
				case '|':
				case '%':
					szOut.append('%');
					int intval = ch;
					szOut.append(String.format("%02x", intval));
					break;
				default:
					szOut.append(ch);
			}
		}
		return szOut.toString();
	}

	/**
	 * Stolen from org.apache.catalina.util.URL
	 * 
	 * @param path
	 */
	public static String relativeUrlPath(String base, String path)
	{
		String normalizedPath = path;

		if( Check.isEmpty(normalizedPath) )
		{
			return base;
		}

		if( normalizedPath.charAt(0) != '/' )
		{
			try
			{
				normalizedPath = new URL(new URL("http", "localhost", base), normalizedPath).getPath();
			}
			catch( MalformedURLException e )
			{
				throw new RuntimeException(e);
			}
		}
		return normalizePath(normalizedPath);
	}

	public static String relativeFilePath(String base, String path)
	{
		if( Check.isEmpty(path) )
		{
			return base;
		}
		return normalizePath(PathUtils.filePath(base, path));
	}

	public static String normalizePath(String path)
	{
		// You can use Path.normalize but depends on the default file system,
		// which may not work in all cases?
		// return Paths.get(normalizedPath).normalize().toString();

		String normalizedPath = path;

		if( normalizedPath.equals("/.") )
		{
			return "/";
		}

		// Normalize the slashes and add leading slash if necessary
		if( normalizedPath.indexOf('\\') >= 0 )
		{
			normalizedPath = normalizedPath.replace('\\', '/');
		}
		if( !normalizedPath.startsWith("/") )
		{
			normalizedPath = "/" + normalizedPath;
		}

		// Resolve occurrences of "//" in the normalized path
		while( true )
		{
			int index = normalizedPath.indexOf("//");
			if( index < 0 )
			{
				break;
			}
			normalizedPath = normalizedPath.substring(0, index) + normalizedPath.substring(index + 1);
		}

		// Resolve occurrences of "/./" in the normalized path
		while( true )
		{
			int index = normalizedPath.indexOf("/./");
			if( index < 0 )
			{
				break;
			}
			normalizedPath = normalizedPath.substring(0, index) + normalizedPath.substring(index + 2);
		}

		// Resolve occurrences of "/../" in the normalized path
		while( true )
		{
			int index = normalizedPath.indexOf("/../");
			if( index < 0 )
			{
				break;
			}
			if( index == 0 )
			{
				throw new RuntimeException("Invalid relative URL reference");
			}
			int index2 = normalizedPath.lastIndexOf('/', index - 1);
			normalizedPath = normalizedPath.substring(0, index2) + normalizedPath.substring(index + 3);
		}

		// Resolve occurrences of "/." at the end of the normalized path
		if( normalizedPath.endsWith("/.") )
		{
			normalizedPath = normalizedPath.substring(0, normalizedPath.length() - 1);
		}

		// Resolve occurrences of "/.." at the end of the normalized path
		if( normalizedPath.endsWith("/..") )
		{
			int index = normalizedPath.length() - 3;
			int index2 = normalizedPath.lastIndexOf('/', index - 1);
			if( index2 < 0 )
			{
				throw new RuntimeException("Invalid relative URL reference");
			}
			normalizedPath = normalizedPath.substring(0, index2 + 1);
		}
		return normalizedPath;
	}

	public static String relativize(String base, String path)
	{
		Path basePath = Paths.get(base);
		Path pathPath = Paths.get(path);
		String relPath = basePath.relativize(pathPath).toString();
		return relPath;
	}

	public static String extension(String filename)
	{
		if( filename == null )
		{
			return null;
		}
		int extInd = filename.lastIndexOf('.');
		if( extInd != -1 )
		{
			return filename.substring(extInd + 1);
		}
		return "";
	}
}
