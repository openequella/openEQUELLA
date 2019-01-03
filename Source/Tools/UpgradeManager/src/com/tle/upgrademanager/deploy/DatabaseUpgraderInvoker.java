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

package com.tle.upgrademanager.deploy;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Lists;
import com.tle.upgrademanager.ManagerConfig;
import com.tle.upgrademanager.helpers.AjaxState;

@SuppressWarnings("nls")
public final class DatabaseUpgraderInvoker
{
	private static final Log LOGGER = LogFactory.getLog(DatabaseUpgraderInvoker.class);

	private final ManagerConfig config;
	private final AjaxState ajax;
	private final String ajaxId;

	public DatabaseUpgraderInvoker(ManagerConfig config, String ajaxId, AjaxState ajax)
	{
		this.config = config;
		this.ajax = ajax;
		this.ajaxId = ajaxId;
	}

	public void migrateSchema() throws Exception
	{
		runMigrator(false);
	}

	public void install() throws Exception
	{
		runMigrator(true);
	}

	public void runMigrator(boolean install) throws Exception
	{
		try
		{
			LOGGER.debug("Launching database-upgrader.jar");

			final File managerDir = config.getManagerDir();
			List<String> args = Lists.newArrayList();
			args.add(config.getJavaBin().getAbsolutePath());
			args.add("-Dlog4j.configuration=file:upgrader-log4j.properties");
			args.add("-classpath");
			args.add(managerDir.getAbsolutePath());
			args.add("-jar");
			args.add(new File(managerDir, "database-upgrader.jar").getAbsolutePath());
			if( install )
			{
				args.add("--install");
			}

			/*
			 * StringBuilder command = new StringBuilder(); boolean first =
			 * true; for( String a : args ) { if( !first ) {
			 * command.append(" "); } command.append(a); first = false; }
			 * System.out.println("Executing " + command.toString());
			 */

			final ProcessBuilder pbuilder = new ProcessBuilder(args);
			pbuilder.directory(config.getManagerDir());

			final Process proc = pbuilder.start();
			int exitStatus;

			final InputStreamReader inErr = new InputStreamReader(proc.getErrorStream());
			final InputStreamReader inStd = new InputStreamReader(proc.getInputStream());

			new UpdateThread(inStd).start();
			new UpdateThread(inErr).start();

			exitStatus = proc.waitFor();
			if( exitStatus != 0 )
			{
				throw new RuntimeException("Exec process returned " + exitStatus + ".");
			}
		}
		catch( Exception ex )
		{
			throw new Exception("Error migrating the database", ex);
		}
		ajax.addHeading(ajaxId, "Installation appears to have migrated successfully");
	}

	public class UpdateThread extends Thread
	{
		private final BufferedReader bufferedReader;

		public UpdateThread(InputStreamReader in)
		{
			bufferedReader = new BufferedReader(in);
		}

		@Override
		public void run()
		{
			try( BufferedReader r = bufferedReader )
			{
				String line = null;
				while( (line = r.readLine()) != null )
				{
					if( line.contains("INFO") )
					{
						ajax.addConsole(ajaxId, line.substring(14 + "INFO".length()));
					}
					else if( line.contains("DEBUG") )
					{
						ajax.addConsole(ajaxId, line.substring(14 + "DEBUG".length()));
					}
					else if( line.contains("ERROR") )
					{
						ajax.addError(ajaxId, line.substring(14 + "ERROR".length()));
					}
					else
					{
						ajax.addConsole(ajaxId, line);
					}
				}
			}
			catch( IOException ex )
			{
				throw new RuntimeException(ex);
			}
		}
	}
}
