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

package com.tle.common.applet;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class KeepAliveTask
{
	private static final Log LOGGER = LogFactory.getLog(KeepAliveTask.class);
	public static final long KEEP_ALIVE_DELAY = TimeUnit.SECONDS.toMillis(10);
	public static final long KEEP_ALIVE_RATE = TimeUnit.MINUTES.toMillis(2);

	private TimerTask task;
	private final SessionHolder holder;

	public KeepAliveTask(SessionHolder holder)
	{
		this.holder = holder;
	}

	public void keepAlive()
	{
		LOGGER.trace("Invoking keep alive");
		holder.getLoginService().keepAlive();
	}

	public void onSchedule()
	{
		LOGGER.info("Scheduling keep alive task");

		// Create a new task and start it.
		Timer daemon = new Timer(true);
		task = new TimerTask()
		{
			@Override
			public void run()
			{
				KeepAliveTask.this.run();
			}
		};
		daemon.scheduleAtFixedRate(task, KEEP_ALIVE_DELAY, KEEP_ALIVE_RATE);
	}

	public boolean isScheduled()
	{
		return task != null;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run()
	{
		try
		{
			keepAlive();
		}
		catch( Exception ex )
		{
			LOGGER.warn("Keep-alive message has failed", ex);
		}
	}

	public void cancel()
	{
		if( task != null )
		{
			LOGGER.info("Cancelling keep alive task");
			task.cancel();
		}
	}
}
