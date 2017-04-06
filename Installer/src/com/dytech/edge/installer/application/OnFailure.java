package com.dytech.edge.installer.application;

import java.io.IOException;

import com.dytech.common.io.FileWrapper;
import com.dytech.devlib.PropBagEx;
import com.dytech.installer.ForeignCommand;
import com.dytech.installer.InstallerException;

public class OnFailure extends ForeignCommand
{
	protected String installDir;

	public OnFailure(PropBagEx commandBag, PropBagEx resultBag) throws InstallerException
	{
		super(commandBag, resultBag);
		installDir = getForeignValue("installDir");
	}

	/*
	 * (non-Javadoc)
	 * @see com.dytech.installer.commands.Command#execute()
	 */
	@Override
	public void execute() throws InstallerException
	{
		getProgress().popupMessage(
			"Fatal Error",
			"The installation process will now make an attempt to restore the system before exiting. \n"
				+ "Please try the following:\n" + "-  Consult the documentation and try again. \n"
				+ "-  Contact support at support@thelearningedge.com.au", true);

		FileWrapper file = new FileWrapper(installDir);
		if( file.exists() )
		{
			try
			{
				file.recursiveDelete();
			}
			catch( IOException ex )
			{
				ex.printStackTrace();
			}
		}
		System.exit(1);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "Installation Failure";
	}
}
