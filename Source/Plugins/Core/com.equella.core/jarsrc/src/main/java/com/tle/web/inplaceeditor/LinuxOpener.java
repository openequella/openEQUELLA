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

package com.tle.web.inplaceeditor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilePermission;
import java.io.FileReader;
import java.io.IOException;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.Permissions;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import com.tle.common.Check;
import com.tle.common.URLUtils;
import com.tle.common.util.ExecUtils;

@SuppressWarnings("nls")
public class LinuxOpener extends AbstractListDialogOpener
{
	private static final Logger LOGGER = Logger.getLogger(LinuxOpener.class.getName());
	private static String XDG_DATA_DIR = "XDG_DATA_DIRS";

	@Override
	protected List<App> getAppList(String filepath, String mimetype) throws IOException
	{
		final Permissions permissions = new Permissions();
		permissions.add(new RuntimePermission("getenv." + XDG_DATA_DIR));
		final AccessControlContext context = new AccessControlContext(new ProtectionDomain[]{new ProtectionDomain(null,
			permissions)});

		EnvPropertyGetter envy = new EnvPropertyGetter(XDG_DATA_DIR, true); // true
																			// =
																			// call
																			// getenv
																			// (and
																			// not
																			// getProperty)

		AccessController.doPrivileged(envy, context);

		String dirs = envy.getPropertyValue();

		if( Check.isEmpty(dirs) )
		{
			throw new UnsupportedOperationException("Linux variation is unsupported - environment variable "
				+ XDG_DATA_DIR + " was blank");
		}

		List<App> apps = new ArrayList<App>();

		boolean haveTriedAndWarnedInOtherDir = false;

		for( String dir : dirs.split(":") )
		{
			if( Check.isEmpty(dir) )
			{
				continue;
			}

			File fd = new File(dir, "applications");
			String[] desktops = getDesktopsForMimeType(fd, mimetype, haveTriedAndWarnedInOtherDir);
			if( desktops != null )
			{
				for( String desktop : desktops )
				{
					if( !Check.isEmpty(desktop) )
					{
						File desktopFile = new File(fd, desktop);
						String name = getValueForFile(desktopFile, "Name", false);
						String exec = getValueForFile(desktopFile, "Exec", false);
						apps.add(new App(name, exec));
					}
				}
			}
			else
			{
				haveTriedAndWarnedInOtherDir = true;
			}
		}

		return apps;
	}

	@Override
	protected void executeApp(App app, String filepath, String mimetype) throws IOException
	{
		// Look at the following for a full list of things we're supposed to do:
		// http://standards.freedesktop.org/desktop-entry-spec/desktop-entry-spec-latest.html#exec-variables
		// space, tab, newline, double- single- quote, backslash, greater,
		// lesser, tilde, bar, amp, semi-colon, dollar, question, hash, left-
		// or right- parentheses, backquote

		// Filenames - wrapping with quotes in cases where there's a space, or
		// other reserved character isn't necessary so long as we manually
		// compose the call to exec as an array, with the application as the
		// first element in the array and the filename (no matter how composed)
		// in the second. In practice, only %f / % F arguments require careful
		// handling: the URL encoding simplifies the %u / %U variants.
		// Depending on the target application, it may require a filepath arg
		// (%f or %F)
		// or a file:// type URL (%u or %U).
		String[] structuredCommand = ExecUtils.splitCommand(app.getExec());
		// We only expect one argument placeholder, but there could be any
		// number of tokens in the app's Exec call, eg: myWierdLinuxApp
		// --verbose %f --purple -Dfoo=bar
		for( int i = 0; i < structuredCommand.length; ++i )
		{
			if( Pattern.compile("%[fF]").matcher(structuredCommand[i]).matches() )
			{
				structuredCommand[i] = filepath;
			}
			else if( Pattern.compile("%[Uu]").matcher(structuredCommand[i]).matches() )
			{
				// URLs need to start with the file protocol
				structuredCommand[i] = "file://" + URLUtils.urlEncode(filepath);
			}
		}

		final Permissions permissions = new Permissions();

		// Access Control seems to explicitly require ALL FILES, not just the
		// executable target ?
		permissions.add(new FilePermission("<<ALL FILES>>", "execute"));

		final AccessControlContext context = new AccessControlContext(new ProtectionDomain[]{new ProtectionDomain(null,
			permissions)});

		final String[] finalCommandArray = structuredCommand;

		AccessController.doPrivileged(new PrivilegedAction<Object>()
		{
			@Override
			public Object run()
			{
				try
				{
					ExecUtils.execAsync(finalCommandArray, null, null);
				}
				catch( Exception ejecta )
				{
					throw new RuntimeException(ejecta);
				}
				return null;
			}
		}, context);

	}

	private String[] getDesktopsForMimeType(File fd, String mimeType, boolean haveTriedAndWarnedInOtherDir)
	{
		String rv = getValueForFile(new File(fd, "mimeinfo.cache"), mimeType, haveTriedAndWarnedInOtherDir);
		return rv == null ? null : rv.split(";");
	}

	private String getValueForFile(File file, String key, boolean haveTriedAndWarnedInOtherDir)
	{
		ValueReaderFromFile valueReaderFromFile = new ValueReaderFromFile(file, key, haveTriedAndWarnedInOtherDir);

		final Permissions permissions = new Permissions();
		permissions.add(new FilePermission(file.getAbsolutePath(), "read"));

		final AccessControlContext context = new AccessControlContext(new ProtectionDomain[]{new ProtectionDomain(null,
			permissions)});

		AccessController.doPrivileged(valueReaderFromFile, context);

		return valueReaderFromFile.getPropertyValue();
	}

	/**
	 * An alternative to an inline anonymous class - this way we can get the
	 * returned property value.
	 */
	public static class EnvPropertyGetter implements PrivilegedAction<Object>
	{
		private String property;
		private String propertyValue;
		private boolean callGetenv;

		protected EnvPropertyGetter(String property, boolean callGetenv)
		{
			this.property = property;
			this.callGetenv = callGetenv;
		}

		protected String getPropertyValue()
		{
			return propertyValue;
		}

		@Override
		public Object run()
		{
			try
			{
				if( callGetenv )
				{
					propertyValue = System.getenv(property);
				}
				else
				{
					propertyValue = System.getProperty(property);
				}
			}
			catch( Exception t )
			{
				throw new RuntimeException(t);
			}
			return null;
		}
	}

	private static class ValueReaderFromFile implements PrivilegedAction<Object>
	{
		private String propertyValue;
		private String key;
		private File fileToRead;
		private boolean haveTriedAndWarnedInOtherDir;

		protected ValueReaderFromFile(File fileToRead, String key, boolean haveTriedAndWarnedInOtherDir)
		{
			this.fileToRead = fileToRead;
			this.key = key;
			this.haveTriedAndWarnedInOtherDir = haveTriedAndWarnedInOtherDir;
		}

		@Override
		public Object run()
		{
			try
			{
				if( !fileToRead.exists() || !fileToRead.canRead() )
				{
					LOGGER.warning(fileToRead.toString() + " does not exist or cannot be read");
					return null;
				}
				else if( haveTriedAndWarnedInOtherDir )
				{
					LOGGER.info(fileToRead.toString() + " found, reading ...");
				}

				try( BufferedReader r = new BufferedReader(new FileReader(fileToRead)) )
				{
					String s = r.readLine();
					while( s != null && propertyValue == null )
					{
						if( s.startsWith(key) )
						{
							propertyValue = s.split("=")[1];
						}
						s = r.readLine();
					}
				}
			}
			catch( Exception t )
			{
				throw new RuntimeException(t);
			}
			return null;
		}

		protected String getPropertyValue()
		{
			return propertyValue;
		}
	}
}
