package com.dytech.installer;

import com.dytech.devlib.PropBagEx;
import com.dytech.gui.ExceptionDialog;

public class Installer
{
	public Installer(PropBagEx script, PropBagEx commands) throws InstallerException
	{
		Wizard w = new Wizard(script, commands);
		w.start();
		PropBagEx results = w.getOutput();

		try
		{
			Interpreter i = new Interpreter(commands, results, new ProgressWindow());
			i.execute();
		}
		catch( InstallerException ex )
		{

			ExceptionDialog dialog = new ExceptionDialog("Error During Installation",
				"An error has occurred performing the installation.  Please try to installing the application again.",
				"v1.0", ex);
			dialog.setTitle("Error During Installation");
			dialog.setVisible(true);
		}
	}

}