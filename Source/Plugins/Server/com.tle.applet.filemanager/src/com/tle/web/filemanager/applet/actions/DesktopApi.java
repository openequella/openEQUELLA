package com.tle.web.filemanager.applet.actions;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@SuppressWarnings("nls")
public class DesktopApi
{
	private static final Log LOGGER = LogFactory.getLog(DesktopApi.class);

	public static boolean browse(URI uri)
	{
		if( openSystemSpecific(uri.toString()) )
		{
			return true;
		}
		if( browseDESKTOP(uri) )
		{
			return true;
		}
		return false;
	}

	public static boolean open(File file)
	{
		if( openSystemSpecific(file.getPath()) )
		{
			return true;
		}
		if( openDESKTOP(file) )
		{
			return true;
		}
		return false;
	}

	public static boolean edit(File file)
	{
		if( openSystemSpecific(file.getPath()) )
		{
			return true;
		}
		if( editDESKTOP(file) )
		{
			return true;
		}
		return false;
	}

	private static boolean openSystemSpecific(String what)
	{
		EnumOS os = getOs();
		if( os.isLinux() )
		{
			if( runCommand("kde-open", "%s", what) )
			{
				return true;
			}
			if( runCommand("gnome-open", "%s", what) )
			{
				return true;
			}
			if( runCommand("xdg-open", "%s", what) )
			{
				return true;
			}
		}

		if( os.isMac() )
		{
			if( runCommand("open", "%s", what) )
			{
				return true;
			}
		}

		if( os.isWindows() )
		{
			if( runCommand("explorer", "%s", what) )
			{
				return true;
			}
		}
		return false;
	}

	private static boolean browseDESKTOP(URI uri)
	{
		LOGGER.info("Trying to use Desktop.getDesktop().browse() with " + uri.toString());
		try
		{
			if( !Desktop.isDesktopSupported() )
			{
				LOGGER.error("Platform is not supported.");
				return false;
			}

			if( !Desktop.getDesktop().isSupported(Desktop.Action.BROWSE) )
			{
				LOGGER.error("BORWSE is not supported.");
				return false;
			}

			Desktop.getDesktop().browse(uri);

			return true;
		}
		catch( Throwable t )
		{
			LOGGER.error("Error using desktop browse.", t);
			return false;
		}
	}

	private static boolean openDESKTOP(File file)
	{
		LOGGER.info("Trying to use Desktop.getDesktop().open() with " + file.toString());
		try
		{
			if( !Desktop.isDesktopSupported() )
			{
				LOGGER.error("Platform is not supported.");
				return false;
			}

			if( !Desktop.getDesktop().isSupported(Desktop.Action.OPEN) )
			{
				LOGGER.error("OPEN is not supported.");
				return false;
			}

			Desktop.getDesktop().open(file);

			return true;
		}
		catch( Throwable t )
		{
			LOGGER.error("Error using desktop open.", t);
			return false;
		}
	}

	private static boolean editDESKTOP(File file)
	{
		LOGGER.info("Trying to use Desktop.getDesktop().edit() with " + file);
		try
		{
			if( !Desktop.isDesktopSupported() )
			{
				LOGGER.error("Platform is not supported.");
				return false;
			}

			if( !Desktop.getDesktop().isSupported(Desktop.Action.EDIT) )
			{
				LOGGER.error("EDIT is not supported.");
				return false;
			}

			Desktop.getDesktop().edit(file);

			return true;
		}
		catch( Throwable t )
		{
			LOGGER.error("Error using desktop edit.", t);
			return false;
		}
	}

	private static boolean runCommand(String command, String args, String file)
	{
		LOGGER.info("Trying to exec:\n   cmd = " + command + "\n   args = " + args + "\n   %s = " + file);
		String[] parts = prepareCommand(command, args, file);

		try
		{
			Process p = Runtime.getRuntime().exec(parts);
			if( p == null )
			{
				return false;
			}

			try
			{
				int retval = p.exitValue();
				if( retval == 0 )
				{
					LOGGER.error("Process ended immediately.");
					return false;
				}
				else
				{
					LOGGER.error("Process crashed.");
					return false;
				}
			}
			catch( IllegalThreadStateException itse )
			{
				LOGGER.error("Process is running.");
				return true;
			}
		}
		catch( IOException e )
		{
			LOGGER.error("Error running command.", e);
			return false;
		}
	}

	private static String[] prepareCommand(String command, String args, String file)
	{
		List<String> parts = new ArrayList<String>();
		parts.add(command);

		if( args != null )
		{
			for( String s : args.split(" ") )
			{
				s = String.format(s, file); // put in the filename thing
				parts.add(s.trim());
			}
		}
		return parts.toArray(new String[parts.size()]);
	}

	public static enum EnumOS
	{
		linux, macos, solaris, unknown, windows;

		public boolean isLinux()
		{
			return this == linux || this == solaris;
		}

		public boolean isMac()
		{
			return this == macos;
		}

		public boolean isWindows()
		{
			return this == windows;
		}
	}

	public static EnumOS getOs()
	{
		String s = System.getProperty("os.name").toLowerCase();

		if( s.contains("win") )
		{
			return EnumOS.windows;
		}

		if( s.contains("mac") )
		{
			return EnumOS.macos;
		}

		if( s.contains("solaris") )
		{
			return EnumOS.solaris;
		}

		if( s.contains("sunos") )
		{
			return EnumOS.solaris;
		}

		if( s.contains("linux") )
		{
			return EnumOS.linux;
		}

		if( s.contains("unix") )
		{
			return EnumOS.linux;
		}
		else
		{
			return EnumOS.unknown;
		}
	}
}