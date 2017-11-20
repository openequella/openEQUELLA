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