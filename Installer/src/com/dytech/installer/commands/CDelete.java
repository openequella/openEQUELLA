package com.dytech.installer.commands;

import java.io.IOException;

import com.dytech.common.io.FileWrapper;
import com.dytech.installer.InstallerException;

public class CDelete extends Command
{
	protected String source;

	public CDelete(String source)
	{
		this.source = source;
	}

	@Override
	public void execute() throws InstallerException
	{
		propogateTaskStarted(1);

		FileWrapper f = new FileWrapper(source);

		try
		{
			f.recursiveDelete();
		}
		catch( IOException ex )
		{
			throw new InstallerException("Fatal Error Deleting File: " + f.getAbsolutePath(), ex);
		}

		propogateSubtaskCompleted();
		propogateTaskCompleted();
	}

	@Override
	public String toString()
	{
		return new String("Deleting " + source);
	}
}