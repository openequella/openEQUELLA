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
