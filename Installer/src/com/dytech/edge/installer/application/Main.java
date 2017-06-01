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

package com.dytech.edge.installer.application;

import java.io.File;
import java.io.InputStream;

import com.dytech.devlib.PropBagEx;
import com.dytech.installer.Installer;
import com.dytech.installer.InstallerException;
import com.dytech.installer.NoWizardInstaller;
import com.tle.common.util.ExecUtils;

public class Main
{
	private String existingResults;

	public void setExistingResults(String existingResults)
	{
		this.existingResults = existingResults;
	}

	public void start() throws InstallerException
	{
		if( ExecUtils.isRunningInJar(Main.class) )
		{
			System.setProperty("user.dir", ExecUtils.findJarFolder(Main.class).getAbsolutePath()); //$NON-NLS-1$
		}

		if( existingResults == null )
		{
			InputStream script = getClass().getResourceAsStream("/script/app-script.xml"); //$NON-NLS-1$
			InputStream commands = getClass().getResourceAsStream("/script/app-commands.xml"); //$NON-NLS-1$

			new Installer(new PropBagEx(script), new PropBagEx(commands));
		}
		else
		{
			File results = new File(existingResults);
			InputStream commands = getClass().getResourceAsStream("/script/app-commands.xml"); //$NON-NLS-1$
			PropBagEx resultBag = new PropBagEx(results);
			PropBagEx commandBag = new PropBagEx(commands);
			new NoWizardInstaller(resultBag, commandBag);
		}
	}
}
