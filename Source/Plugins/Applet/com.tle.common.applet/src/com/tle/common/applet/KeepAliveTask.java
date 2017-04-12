/*
 * Created on Apr 1, 2005
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
