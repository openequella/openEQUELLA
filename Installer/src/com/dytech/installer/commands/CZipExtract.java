package com.dytech.installer.commands;

import java.io.File;
import java.io.IOException;

import com.dytech.common.io.ZipUtils;
import com.dytech.installer.InstallerException;

public class CZipExtract extends Command
{
	protected String source;
	protected String destination;
	protected String pattern;

	public CZipExtract(String source, String destination)
	{
		this(source, destination, ".*");
	}

	public CZipExtract(String source, String destination, String pattern)
	{
		this.source = source;
		this.destination = destination;
		this.pattern = pattern;
	}

	@Override
	public void execute() throws InstallerException
	{
		propogateTaskStarted(1);

		if( source.endsWith("*") )
		{
			source = source.substring(0, source.length() - 1);
			File f = new File(source);
			if( !f.exists() )
			{
				throw new InstallerException(f.toString() + " does not exist");
			}

			if( !f.isDirectory() )
			{
				throw new InstallerException(f.toString() + " is not a directory");
			}

			File[] children = f.listFiles();
			if( children.length == 0 )
			{
				throw new InstallerException(f.toString() + " does not have any files");
			}

			source = children[0].toString();
		}

		try
		{
			ZipUtils.extract(new File(source), ZipUtils.createZipFilter(pattern), new File(destination));
		}
		catch( IOException ex )
		{
			final String message = "" + "Fatal Error Extracting File From Zip:\n" + "Zip Source = " + source + '\n'
				+ "Destination = " + destination + '\n' + "Pattern = " + pattern;
			throw new InstallerException(message, ex);
		}

		propogateSubtaskCompleted();
		propogateTaskCompleted();
	}

	@Override
	public String toString()
	{
		return new String("Extracting from " + source + " to " + destination);
	}
}
