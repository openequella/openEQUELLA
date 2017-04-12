package com.dytech.edge.installer.application;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;

import com.dytech.common.text.NumberStringComparator;
import com.dytech.devlib.PropBagEx;
import com.dytech.installer.Callback;
import com.dytech.installer.Wizard;

/*
 * @author Nicholas Read
 */
public class FindJava implements Callback
{
	@SuppressWarnings("nls")
	private static final String[] FOLDER_STARTS = {"jdk1.7", "java"};
	@SuppressWarnings("nls")
	private static final String[] SEARCH_PATHS = {"c:/", "/usr", "/usr/local", "/opt", "c:/devenv/sdk/",
			"c:/Program Files/Java", "/usr/lib/jvm"};

	/**
	 * Class constructor.
	 */
	public FindJava()
	{
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see com.dytech.installer.Callback#task(com.dytech.installer.Wizard)
	 */
	@Override
	public void task(Wizard installer)
	{
		PropBagEx output = installer.getOutputNow();
		String platform = output.getNode("installer/platform"); //$NON-NLS-1$
		PropBagEx defaults = installer.getDefaults();

		int page = installer.getCurrentPageNumber();
		String javaHome = ""; //$NON-NLS-1$
		String installPath = ""; //$NON-NLS-1$

		if( platform.equals("mac") ) //$NON-NLS-1$
		{
			page += 2;
		}
		else if( platform.startsWith("win") ) //$NON-NLS-1$
		{
			page += 1;
		}
		// starts with solaris as it might be solaris-sparc or solaris-x86
		else if( platform.startsWith("linux") || platform.startsWith("solaris") ) //$NON-NLS-1$ //$NON-NLS-2$
		{
			page += 1;
		}

		if( defaults == null || (defaults.getNode("java/jdk") == null || defaults.getNode("java/jdk").equals("")) ) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		{
			if( platform.equals("mac") ) //$NON-NLS-1$
			{
				javaHome = "/Library/Java/Home"; //$NON-NLS-1$
			}
			else if( platform.startsWith("win") || platform.startsWith("linux") //$NON-NLS-1$ //$NON-NLS-2$
				|| platform.startsWith("solaris") ) //$NON-NLS-1$
			{
				javaHome = findJava(installer);
			}
		}
		else
		{
			javaHome = defaults.getNode("java/jdk"); //$NON-NLS-1$
		}

		if( defaults == null || (defaults.getNode("install.path") == null || defaults.getNode("install.path") //$NON-NLS-1$ //$NON-NLS-2$
			.equals("")) ) //$NON-NLS-1$
		{
			if( platform.equals("mac") ) //$NON-NLS-1$
			{
				installPath = "/Applications/equella"; //$NON-NLS-1$
			}
			else if( platform.startsWith("linux") || platform.startsWith("solaris") ) //$NON-NLS-1$ //$NON-NLS-2$
			{
				installPath = "/usr/local/equella"; //$NON-NLS-1$
			}
			else if( platform.startsWith("win") ) //$NON-NLS-1$
			{
				installPath = "c:\\equella"; //$NON-NLS-1$
			}
		}
		else
		{
			installPath = defaults.getNode("install.path"); //$NON-NLS-1$
		}

		output.setNode("install.path", installPath); //$NON-NLS-1$
		output.setNode("java/jdk", javaHome); //$NON-NLS-1$

		installer.gotoPage(page);
	}

	/**
	 * This attempts to search for Java at various paths on the harddrive.
	 */
	private String findJava(Wizard installer)
	{
		for( int i = 0; i < SEARCH_PATHS.length; i++ )
		{
			String path = findJavaInPath(SEARCH_PATHS[i]);
			if( path != null )
			{
				return path;
			}
		}
		return ""; //$NON-NLS-1$
	}

	/**
	 * Looks for the SDK in the given path. If there are multiple matching
	 * directories, it will try and grab the one with the highest revision. For
	 * example, j2sdk1.4.2_07 will be selected rather than j2sdk1.4.2_04.
	 */
	private String findJavaInPath(String rootPath)
	{
		File root = new File(rootPath);
		if( root.exists() )
		{
			// Find all directories starting with 'j2sdk1.4.2'
			String[] listing = root.list(new FilenameFilter()
			{
				/*
				 * (non-Javadoc)
				 * @see java.io.FilenameFilter#accept(java.io.File,
				 * java.lang.String)
				 */
				@Override
				public boolean accept(File dir, String name)
				{
					for( String start : FOLDER_STARTS )
					{
						if( name.startsWith(start) )
						{
							return true;
						}
					}
					return false;
				}
			});

			if( listing.length > 0 )
			{
				Arrays.sort(listing, new NumberStringComparator<String>());
				String bestSdk = listing[listing.length - 1];
				return new File(root, bestSdk).getAbsolutePath();
			}
		}
		return null;
	}
}
