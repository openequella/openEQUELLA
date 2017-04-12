package com.dytech.installer.commands;

import java.io.IOException;

import com.dytech.common.io.FileWrapper;
import com.dytech.installer.InstallerException;

public class CCopy extends Command
{
	protected String source;
	protected String destination;
	protected boolean overwrite;

	public CCopy(String source, String destination, boolean overwrite)
	{
		this.source = source;
		this.destination = destination;
		this.overwrite = overwrite;
	}

	@Override
	public void execute() throws InstallerException
	{
		propogateTaskStarted(1);

		FileWrapper s = new FileWrapper(source);
		FileWrapper d = new FileWrapper(destination);

		try
		{
			s.copy(d, overwrite);
		}
		catch( IOException ex )
		{
			final String message = "" + "Fatal Error Copying File:\n" + "Source = " + s.getAbsolutePath() + '\n'
				+ "Destination = " + d.getAbsolutePath() + '\n' + "Overwrite = " + Boolean.toString(overwrite);
			throw new InstallerException(message, ex);
		}

		propogateSubtaskCompleted();
		propogateTaskCompleted();
	}

	@Override
	public String toString()
	{
		return new String("Copying " + source + " to " + destination);
	}
}