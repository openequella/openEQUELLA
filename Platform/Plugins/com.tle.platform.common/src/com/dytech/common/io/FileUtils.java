/*
 * Created on Sep 22, 2005
 */
package com.dytech.common.io;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@SuppressWarnings("nls")
public final class FileUtils
{
	/**
	 * Recursively delete files and directories. Unlike the standard
	 * <code>delete</code>, method, this implementation does not require that
	 * directories be empty.
	 */
	public static void delete(File f) throws IOException
	{
		if( !f.exists() )
		{
			return;
		}
		if( f.isDirectory() )
		{
			File[] files = f.listFiles();
			for( int i = 0; i < files.length; i++ )
			{
				delete(files[i]);
			}
		}

		if( !f.delete() )
		{
			throw new IOException("Could not delete file: " + f.toString());
		}
	}

	public static List<String> grep(final File root, final String pattern, final boolean filesOnly)
	{
		final List<String> results = new ArrayList<String>();
		grep(root, pattern, new GrepFunctor()
		{
			@Override
			public void matched(File file, String relFilepath)
			{
				if( !filesOnly || file.isFile() )
				{
					results.add(relFilepath);
				}
			}
		});
		return results;
	}

	public static void grep(final File root, String pattern, final GrepFunctor functor)
	{
		if( !root.exists() )
		{
			return;
		}
		boolean recurse = (pattern.indexOf('/') != -1) || pattern.contains("**");
		// Literally match periods
		pattern = pattern.replaceAll("\\.", "\\\\.");

		// Question marks match a possible character - not zero or one of the
		// previous character
		pattern = pattern.replaceAll("\\?", ".?");//$NON-NLS-2$

		// A single asterisk (ensure not double) match any
		// non-directory-separator character
		pattern = pattern.replaceAll("(?<!\\*)(?<!])\\*(?!\\*)", "[^/]*");

		// Special case for pattern only being a double asterisk
		pattern = pattern.replaceFirst("^\\*\\*$", ".*");

		// Special case for double asterisk at start. Match anything in the
		// current directory, or
		// allow for any number of parent directories. Note the regex tests for
		// nothing or something.
		pattern = pattern.replaceFirst("^\\*\\*/", "(?:|.*/)");

		// Same case for end, but also the additional case where ending with a
		// directory separator
		// is equivalent to ending with a double asterisk, ie, "/blah/" is equal
		// to "/blah/**"
		pattern = pattern.replaceFirst("/(?:\\*\\*)$", "/.*");

		// Allow a double asterisk directory path to match anything in the
		// current any directories
		pattern = pattern.replaceAll("/\\*\\*/", "/(?:|.*/)");

		// If there are any double asterisks left, then the pattern is incorrect
		// as a double asterisk
		// should have only occurred at the start or end of the pattern, or
		// surrounded immediately by
		// directory separators. For example, "/blah**/" is invalid.
		if( pattern.contains("**") )
		{
			throw new RuntimeException("Double askterisk pattern may only be used at the start or"
				+ " end of the pattern, or surrounded immediately by directory separators.  For"
				+ " example, \"**/one/**/two/**\" is valid, while \"one/two**three/four\" is" + " invalid.");
		}

		grep(root, null, Pattern.compile(pattern), functor, recurse);
	}

	/**
	 * This method has been copied straight from FileSystemServiceImpl
	 */
	private static void grep(final File current, final String path, final Pattern regex, GrepFunctor functor,
		boolean recurse)
	{
		for( final File f : current.listFiles() )
		{
			final String newPath = (path != null ? path + "/" : "") + f.getName();

			if( regex.matcher(newPath).matches() )
			{
				functor.matched(f, newPath);
			}

			if( recurse && f.isDirectory() )
			{
				grep(f, newPath, regex, functor, true);
			}
		}
	}

	public interface GrepFunctor
	{
		void matched(File file, String relFilepath);
	}

	private FileUtils()
	{
		throw new Error();
	}
}
