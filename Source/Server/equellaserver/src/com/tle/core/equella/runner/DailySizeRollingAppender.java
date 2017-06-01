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

package com.tle.core.equella.runner;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.helpers.CountingQuietWriter;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.helpers.OptionConverter;
import org.apache.log4j.spi.LoggingEvent;

@SuppressWarnings("nls")
public class DailySizeRollingAppender extends FileAppender
{
	private final DateFormat DIRECTORY_NAME = new SimpleDateFormat("yyyy-MM-dd"); //$NON-NLS-1$
	private static final Pattern FILE_PATTERN = Pattern.compile("(.*)/(.*?)\\.(.*?)"); //$NON-NLS-1$

	private long maxFileSize;
	private int maxBackupIndex;
	private long nextRollTime;
	private File directory;
	private File datedDir;
	private String logName;
	private String logExtension;

	public DailySizeRollingAppender()
	{
		maxFileSize = 0xa00000L;
		maxBackupIndex = 20;
		nextRollTime = 0L;
	}

	public int getMaxBackupIndex()
	{
		return maxBackupIndex;
	}

	public long getMaximumFileSize()
	{
		return maxFileSize;
	}

	public void rollDirectory()
	{
		LogLog.debug("Rolling Directory");
		LogLog.debug("New directory is = " + datedDir);
		String newFileName = (new File(datedDir, logName + '.' + logExtension)).getAbsolutePath();
		try
		{
			setFile(newFileName, false, bufferedIO, bufferSize);
		}
		catch( IOException e )
		{
			LogLog.error("setFile(" + newFileName + ", false) call failed.", e);
		}
	}

	public void rollFile()
	{
		LogLog.debug("Rolling File");
		LogLog.debug("rolling over count=" + ((CountingQuietWriter) qw).getCount());
		LogLog.debug("maxBackupIndex=" + maxBackupIndex);
		if( maxBackupIndex > 0 )
		{
			File file = new File(datedDir, logName + '.' + maxBackupIndex + '.' + logExtension);
			if( file.exists() )
			{
				boolean wasDeleted = file.delete();
				if( !wasDeleted )
				{
					LogLog.warn("Failed to delete " + file.getAbsolutePath());
				}
			}
			File target;
			for( int i = maxBackupIndex - 1; i >= 1; i-- )
			{
				file = new File(datedDir, logName + '.' + i + '.' + logExtension);
				if( file.exists() )
				{
					target = new File(datedDir, logName + '.' + (i + 1) + '.' + logExtension);
					LogLog.debug("Renaming file " + file + " to " + target);
					boolean wasRenamed = file.renameTo(target);
					if( !wasRenamed )
					{
						LogLog.warn("Failed to rename " + file.getAbsolutePath() + " to " + target.getAbsolutePath());
					}
				}
			}

			closeFile();
			target = new File(datedDir, logName + ".1." + logExtension);
			file = new File(datedDir, logName + '.' + logExtension);
			LogLog.debug("Renaming file " + file + " to " + target);
			file.renameTo(target);
		}
		String newFileName = (new File(datedDir, logName + '.' + logExtension)).getAbsolutePath();
		try
		{
			setFile(newFileName, false, bufferedIO, bufferSize);
		}
		catch( IOException e )
		{
			LogLog.error("setFile(" + newFileName + ", false) call failed.", e);
		}
	}

	@Override
	public synchronized void setFile(String fileName, boolean append, boolean bufferedIO, int bufferSize)
		throws IOException
	{
		super.setFile(fileName, append, this.bufferedIO, this.bufferSize);
		if( append )
		{
			File f = new File(fileName);
			((CountingQuietWriter) qw).setCount(f.length());
		}
	}

	public void setMaxBackupIndex(int maxBackups)
	{
		maxBackupIndex = maxBackups;
	}

	public void setMaximumFileSize(long maxFileSize)
	{
		this.maxFileSize = maxFileSize;
	}

	public void setMaxFileSize(String value)
	{
		maxFileSize = OptionConverter.toFileSize(value, maxFileSize + 1L);
	}

	@Override
	protected void setQWForFiles(Writer writer)
	{
		qw = new CountingQuietWriter(writer, errorHandler);
	}

	@Override
	protected void subAppend(LoggingEvent event)
	{
		LogLog.debug("subAppend");
		long now = System.currentTimeMillis();
		if( now >= nextRollTime )
		{
			LogLog.debug("Have to roll directory");
			calculateRollOverTime();
			rollDirectory();
		}
		else if( getFile() != null && ((CountingQuietWriter) qw).getCount() >= maxFileSize )
		{
			LogLog.debug("Have to roll file");
			rollFile();
		}
		LogLog.debug("Calling Super Sub Append");
		super.subAppend(event);
	}

	private void calculateRollOverTime()
	{
		Calendar c = Calendar.getInstance();
		datedDir = new File(directory, DIRECTORY_NAME.format(c.getTime()));
		boolean madeDirs = datedDir.mkdirs();
		if( !(madeDirs || datedDir.exists()) )
		{
			LogLog.warn("Could not create/confirm directory " + datedDir.getAbsolutePath());
		}
		c.add(5, 1);
		c.set(10, 0);
		c.set(12, 0);
		c.set(13, 0);
		c.set(9, 0);
		nextRollTime = c.getTimeInMillis();
	}

	@Override
	public void setFile(String file)
	{
		Matcher m = FILE_PATTERN.matcher(file);
		if( m.matches() )
		{
			directory = new File(m.group(1));
			logName = m.group(2);
			logExtension = m.group(3);
			calculateRollOverTime();
			String newFileName = (new File(datedDir, logName + '.' + logExtension)).getAbsolutePath();
			super.setFile(newFileName);
		}
	}

	public static void main(String args[])
	{
		Logger logger = Logger.getLogger(DailySizeRollingAppender.class);
		do
		{
			logger.info("This is an info statement");
			logger.warn("This is a warning");
			logger.error("This is an error");
			try
			{
				throw new Exception();
			}
			catch( Exception ex )
			{
				logger.fatal("This is fatal", ex);
			}
		}
		while( true );
	}
}
