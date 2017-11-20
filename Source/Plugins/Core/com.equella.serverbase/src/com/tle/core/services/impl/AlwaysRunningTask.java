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

package com.tle.core.services.impl;

import org.apache.log4j.Logger;

/**
 * "Always Running" tasks will never exit unless joining a cluster means there
 * are now two of them, in which case one will be destroyed. Typically, these
 * tasks wait for messages that they react to.
 */
@SuppressWarnings("nls")
public abstract class AlwaysRunningTask<T> extends Task
{
	private final Logger LOGGER = Logger.getLogger(getClass());

	@Override
	public Void call() throws Exception
	{
		Thread.currentThread().setName("AlwaysRunningTask-" + getClass().getName());

		try
		{
			init();
		}
		catch( Exception ex )
		{
			LOGGER.error("Error starting task", ex);
		}

		while( !isShutdown() )
		{
			try
			{
				T v = waitFor();

				// We're not supposed to exist anymore - die!
				if( isShutdown() )
				{
					return doShutdown();
				}

				runTask(v);
			}
			catch( InterruptedException ie )
			{
				if( isShutdown() )
				{
					return doShutdown();
				}
				else
				{
					LOGGER.error("Got interrupted but not asked to shut down", ie);
				}
			}
			catch( Exception ex )
			{
				LOGGER.error("Error running task", ex);
			}
		}
		return doShutdown();
	}

	private Void doShutdown()
	{
		LOGGER.warn("Asked to shut down task:" + getTaskId());
		return null;
	}

	protected void init() throws Exception
	{
		// Nothing by default
	}

	/**
	 * Where the global task should do any blocking operations.
	 */
	protected abstract T waitFor() throws Exception;

	/**
	 * Where work should be done and then returned (do not do while(true)!). The
	 * value of waitFor() is passed in.
	 */
	protected abstract void runTask(T waitedFor) throws Exception;
}