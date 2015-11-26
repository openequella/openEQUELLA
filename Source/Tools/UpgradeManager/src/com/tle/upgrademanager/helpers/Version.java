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

package com.tle.upgrademanager.helpers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;

import com.google.common.base.Function;
import com.tle.upgrademanager.ManagerConfig;
import com.tle.upgrademanager.Utils;
import com.tle.upgrademanager.handlers.PagesHandler.WebVersion;

@SuppressWarnings("nls")
public class Version
{

	private final ManagerConfig config;

	public Version(ManagerConfig config)
	{
		this.config = config;
	}

	public SortedSet<WebVersion> getVersions()
	{
		final SortedSet<WebVersion> versions = new TreeSet<WebVersion>(Utils.VERSION_COMPARATOR);

		final File upgradeFolder = config.getUpdatesDir();
		if( upgradeFolder.exists() )
		{
			for( String file : upgradeFolder.list() )
			{
				if( file != null && Utils.VERSION_EXTRACT.matcher(file).matches() )
				{
					versions.add(getWebVersionFromFile(file));
				}
			}
		}
		else
		{
			throw new RuntimeException("No upgrades folder found");
		}

		return versions;
	}

	private WebVersion getWebVersionFromFile(String fn)
	{
		return new WebVersion(DISPLAY_NAME_ONLY.apply(fn), VERSION_NUMBER_ONLY.apply(fn), FULL_FILENAME.apply(fn));
	}

	public static final Function<String, String> FULL_FILENAME = new Function<String, String>()
	{
		@Override
		public String apply(String filename)
		{
			Matcher m1 = Utils.VERSION_EXTRACT.matcher(filename);
			return m1.matches() ? filename : null;
		}
	};

	public static final Function<String, String> VERSION_NUMBER_ONLY = new Function<String, String>()
	{
		@Override
		public String apply(String filename)
		{
			Matcher m1 = Utils.VERSION_EXTRACT.matcher(filename);
			if( m1.matches() )
			{
				return m1.group(1);
			}
			return null;
		}
	};

	public static final Function<String, String> DISPLAY_NAME_ONLY = new Function<String, String>()
	{
		@Override
		public String apply(String filename)
		{
			Matcher m = Utils.VERSION_EXTRACT.matcher(filename);
			if( m.matches() )
			{
				return m.group(2);
			}
			return null;
		}
	};

	public WebVersion getDeployedVersion()
	{
		WebVersion version = new WebVersion();
		File versionFile = new File(getVersionPropertiesDirectory(), "version.properties");
		try( FileInputStream in = new FileInputStream(versionFile) )
		{
			Properties p = new Properties();
			p.load(in);
			version.setDisplayName(p.getProperty("version.display"));
			version.setMmr(p.getProperty("version.mmr"));
			version.setFilename(MessageFormat.format("tle-upgrade-{0} ({1}).zip", p.getProperty("version.mmr"),
				p.getProperty("version.display")));
		}
		catch( IOException ex )
		{
			version.setDisplayName(Utils.UNKNOWN_VERSION);
		}
		return version;
	}

	private File getVersionPropertiesDirectory()
	{
		return new File(config.getInstallDir(), Utils.EQUELLASERVER_DIR);
	}

	public File getUpgradeFile(String filename)
	{
		File vdir = config.getUpdatesDir();
		if( vdir.exists() )
		{
			File file = new File(vdir, filename);
			if( file.exists() )
			{
				return file;
			}
		}

		return null;
	}
}
