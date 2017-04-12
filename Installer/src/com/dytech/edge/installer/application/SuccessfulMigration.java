package com.dytech.edge.installer.application;

import com.dytech.devlib.PropBagEx;
import com.dytech.installer.ForeignCommand;
import com.dytech.installer.InstallerException;

public class SuccessfulMigration extends ForeignCommand
{
	public SuccessfulMigration(PropBagEx commandBag, PropBagEx resultBag) throws InstallerException
	{
		super(commandBag, resultBag);
	}

	/*
	 * (non-Javadoc)
	 * @see com.dytech.installer.commands.Command#execute()
	 */
	@Override
	public void execute() throws InstallerException
	{
		StringBuilder message = new StringBuilder();
		message.append("Data has been migrated successfully!\n\n");

		getProgress().popupMessage("Service Notes", message.toString(), false);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "Final installation instructions";
	}
}
