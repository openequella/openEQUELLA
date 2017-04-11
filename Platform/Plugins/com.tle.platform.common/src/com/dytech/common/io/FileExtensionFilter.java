/*
 * Created on Oct 18, 2005
 */
package com.dytech.common.io;

import java.io.File;
import java.io.FileFilter;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("nls")
public class FileExtensionFilter implements FileFilter
{
	private static final Pattern REGEX = Pattern.compile(".*\\.(\\w+)");

	private final Set<String> extensions = new HashSet<String>();

	public FileExtensionFilter(String... extensions)
	{
		for( String extension : extensions )
		{
			addExtension(extension);
		}
	}

	public void addExtension(String extension)
	{
		synchronized( extensions )
		{
			extensions.add(extension.toLowerCase());
		}
	}

	public void removeExtension(String extension)
	{
		synchronized( extensions )
		{
			extensions.remove(extension.toLowerCase());
		}
	}

	@Override
	public boolean accept(File f)
	{
		if( f.isDirectory() )
		{
			return true;
		}
		else
		{
			Matcher match = REGEX.matcher(f.getName());
			if( !match.matches() || match.groupCount() != 1 )
			{
				return false;
			}
			else
			{
				String extension = match.group(1).toLowerCase();
				synchronized( extensions )
				{
					return extensions.contains(extension);
				}
			}
		}
	}

	// // COMMON INSTANCES
	// ///////////////////////////////////////////////////////////////

	private static final Object CREATION_LOCK = new Object();

	private static FileExtensionFilter imagesFilter;
	private static FileExtensionFilter htmlFilter;
	private static FileExtensionFilter xmlFilter;
	private static FileExtensionFilter xsltFilter;
	private static FileExtensionFilter zipFilter;

	public static FileExtensionFilter IMAGES()
	{
		synchronized( CREATION_LOCK )
		{
			if( imagesFilter == null )
			{
				imagesFilter = new FileExtensionFilter();
				imagesFilter.addExtension("gif");
				imagesFilter.addExtension("jpg");
				imagesFilter.addExtension("jpeg");
				imagesFilter.addExtension("png");
				imagesFilter.addExtension("bmp");
			}
			return imagesFilter;
		}
	}

	public static FileExtensionFilter HTML()
	{
		synchronized( CREATION_LOCK )
		{
			if( htmlFilter == null )
			{
				htmlFilter = new FileExtensionFilter();
				htmlFilter.addExtension("html");
				htmlFilter.addExtension("htm");
			}
			return htmlFilter;
		}
	}

	public static FileExtensionFilter ZIP()
	{
		synchronized( CREATION_LOCK )
		{
			if( zipFilter == null )
			{
				zipFilter = new FileExtensionFilter();
				zipFilter.addExtension("zip");
			}
			return zipFilter;
		}
	}

	public static FileExtensionFilter XML()
	{
		synchronized( CREATION_LOCK )
		{
			if( xmlFilter == null )
			{
				xmlFilter = new FileExtensionFilter();
				xmlFilter.addExtension("xml");
			}
			return xmlFilter;
		}
	}

	public static FileExtensionFilter XSLT()
	{
		synchronized( CREATION_LOCK )
		{
			if( xsltFilter == null )
			{
				xsltFilter = new FileExtensionFilter();
				xsltFilter.addExtension("xslt");
				xsltFilter.addExtension("xsl");
			}
			return xsltFilter;
		}
	}
}
