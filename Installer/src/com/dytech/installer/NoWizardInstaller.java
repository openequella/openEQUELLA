package com.dytech.installer;

import com.dytech.devlib.PropBagEx;

public class NoWizardInstaller
{
	public NoWizardInstaller(PropBagEx results, PropBagEx commands) throws InstallerException
	{
		try
		{
			Interpreter i = new Interpreter(commands, results, new CommandLineProgress());
			i.execute();
		}
		catch( InstallerException ex )
		{
			System.err.println("======================================");
			System.err.println("ERROR DURING INSTALLATION");
			System.err
				.println("An error has occurred performing the installation.  Please try to installing the application again.");
			System.err.println("======================================");
			ex.printStackTrace();
		}
	}
}