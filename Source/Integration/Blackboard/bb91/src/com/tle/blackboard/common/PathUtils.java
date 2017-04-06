package com.tle.blackboard.common;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Strings;

@SuppressWarnings("nls")
public class PathUtils
{
	private PathUtils()
	{
		throw new Error();
	}

	public static String urlPath(String... urlBits)
	{
		return filePath(urlBits);
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
			if( !Strings.isNullOrEmpty(bit) )
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
			if( !Strings.isNullOrEmpty(bit) )
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

	//
	// /**
	// * Breaks filename into a path with no file extension, and the extension
	// * (with no dot)
	// *
	// * @param filename
	// * @return
	// */
	// public static Pair<String, String> fileParts(String filename)
	// {
	// String fullSrcPath = filename;
	// String fileNoExt = fullSrcPath;
	// String ext = "";
	// int dot = fullSrcPath.lastIndexOf('.');
	// if( dot > 0 )
	// {
	// fileNoExt = fullSrcPath.substring(0, dot);
	// ext = fullSrcPath.substring(dot + 1);
	// }
	// return new Pair<String, String>(fileNoExt, ext);
	// }

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
	public static String relativePath(String base, String path)
	{
		// Create a place for the normalized path
		String normalizedPath = path;

		if( Strings.isNullOrEmpty(normalizedPath) )
		{
			return base;
		}
		else if( normalizedPath.charAt(0) != '/' )
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

	public static String extension(String filename)
	{
		int extInd = filename.lastIndexOf('.');
		if( extInd != -1 )
		{
			return filename.substring(extInd + 1);
		}
		return "";
	}
}
