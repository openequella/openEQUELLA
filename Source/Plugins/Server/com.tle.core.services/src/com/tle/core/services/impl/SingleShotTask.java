package com.tle.core.services.impl;

import org.apache.log4j.Logger;

/**
 * "Single Shot" tasks always exit after running once.
 */
public abstract class SingleShotTask extends Task
{
	private static final Logger LOGGER = Logger.getLogger(SingleShotTask.class);

	@SuppressWarnings("nls")
	@Override
	public final Void call() throws Exception
	{
		Thread.currentThread().setName("SingleShotTask-" + getClass().getName());
		try
		{
			runTask();
		}
		catch( Throwable t )
		{
			LOGGER.error("Error running task", t);
			throw t;
		}
		return null;
	}

	public abstract void runTask() throws Exception;
}