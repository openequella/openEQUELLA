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