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

package com.tle.upgrademanager;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.text.MessageFormat;
import java.util.Properties;

import com.tle.common.util.EquellaConfig;

@SuppressWarnings("nls")
public class ManagerConfig extends EquellaConfig
{
	private final File updatesDir;

	private final ManagerDetails managerDetails;

	public ManagerConfig(File installPath, InputStream inputStream) throws IOException
	{
		super(installPath);
		updatesDir = new File(managerDir, "updates"); //$NON-NLS-1$
		managerDetails = new ManagerDetails(new File(managerDir, "config.properties"), inputStream); //$NON-NLS-1$
	}

	public File getUpdatesDir()
	{
		return updatesDir;
	}

	public ManagerDetails getManagerDetails()
	{
		return managerDetails;
	}

	public class ManagerDetails
	{
		private static final String SERVER_PORT_KEY = "server.port";
		private static final String LOADING_TIME_OUT_KEY = "loading.time.out";

		private static final String LOADING_TIME_OUT_DEFAULT = "6000";
		private static final String SERVER_PORT_DEFAULT = "3000";

		private static final String VERSION_MMR = "version.mmr";
		private static final String VERSION_DISPLAY = "version.display";
		private static final String VERSION_COMMIT = "version.commit";

		private final File managerConfigFile;
		private final Properties props;

		// sort-of final
		private int managerPort;
		private long loadingTimeout;
		private String upgradeHost;

		private final String fullVersion;

		protected ManagerDetails(File managerConfigFile, InputStream inputStream) throws IOException
		{
			this.managerConfigFile = managerConfigFile;
			this.props = new Properties();

			load();

			if( inputStream != null )
			{
				final Properties vp = new Properties();
				vp.load(inputStream);
				fullVersion = MessageFormat.format("{0} {1} ({2})", vp.getProperty(VERSION_MMR),
					vp.getProperty(VERSION_DISPLAY), vp.getProperty(VERSION_COMMIT));
			}
			else
			{
				fullVersion = "Unknown";
			}
		}

		public final void load() throws IOException
		{
			try( Reader reader = new FileReader(this.managerConfigFile) )
			{
				props.load(reader);

				managerPort = Integer.parseInt(props.getProperty(SERVER_PORT_KEY, SERVER_PORT_DEFAULT));
				loadingTimeout = Long.parseLong(props.getProperty(LOADING_TIME_OUT_KEY, LOADING_TIME_OUT_DEFAULT));
			}
		}

		public int getManagerPort()
		{
			return managerPort;
		}

		public long getLoadingTimeout()
		{
			return loadingTimeout;
		}

		public String getUpgradeHost()
		{
			return upgradeHost;
		}

		public String getFullVersion()
		{
			return fullVersion;
		}
	}
}
