package com.dytech.installer.commands;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import com.dytech.common.io.DevNullOutputStream;
import com.dytech.installer.InstallerException;
import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;
import com.tle.common.util.ExecUtils;
import com.tle.common.util.ExecUtils.ExecResult;

public class CExecute extends Command
{
	protected String command;
	protected String[] env;
	protected File path;
	protected int secondsToWait;

	public CExecute(String command, String path, String env, int secondsToWait)
	{
		this.command = command;
		this.secondsToWait = secondsToWait;

		if( env == null )
			this.env = null;
		else
			this.env = new String[]{env};

		if( path == null )
			this.path = null;
		else
			this.path = new File(path);
	}

	@Override
	public void execute() throws InstallerException
	{
		propogateTaskStarted(2);

		final ExecResult result = ExecUtils.exec(command, env, path);
		propogateSubtaskCompleted();

		if( result.getExitStatus() != 0 )
		{
			throw new InstallerException("Program returned bad result = " + result);
		}

		propogateSubtaskCompleted();
		propogateTaskCompleted();
	}

	@Override
	public String toString()
	{
		return new String("Executing " + command);
	}

	private static class StreamReader extends Thread
	{
		private final InputStream input;
		private final Process proc;
		private boolean finished;

		public StreamReader(InputStream input, Process proc)
		{
			this.input = input;
			this.proc = proc;
		}

		@Override
		public void run()
		{
			try
			{
				ByteStreams.copy(input, new DevNullOutputStream());
			}
			catch( IOException e )
			{
			}
			finally
			{
				try
				{
					Closeables.close(input, true);
				}
				catch( IOException e )
				{
					// Ignore
				}
				finished = true;
			}
			synchronized( proc )
			{
				proc.notify();
			}
		}

		public boolean isFinished()
		{
			return finished;
		}
	}
}