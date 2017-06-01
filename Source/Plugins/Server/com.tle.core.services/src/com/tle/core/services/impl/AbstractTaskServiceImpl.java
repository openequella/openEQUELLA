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

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Named;

import org.java.plugin.registry.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tle.common.NamedThreadFactory;
import com.tle.core.plugins.PluginService;
import com.tle.core.plugins.PluginTracker;

/**
 * @author Aaron
 *
 */
public abstract class AbstractTaskServiceImpl implements PrivateTaskService
{
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractTaskServiceImpl.class);

	@Inject
	@Named("taskService.maxConcurrentTasks")
	private int maxConcurrentTasks;
	@Inject
	private PluginService pluginService;
	@Inject
	private PluginTracker<Object> coreTasks;

	protected void waitForDependencies(ClusteredTask task)
	{
		final Collection<String> waitFor;
		final String globalId = task.getGlobalId();
		final Map<String, Extension> extensionMap = coreTasks.getExtensionMap();

		waitFor = new HashSet<String>();
		for( Entry<String, Extension> ext : extensionMap.entrySet() )
		{
			final String key = ext.getKey();
			final Extension extension = ext.getValue();

			boolean matched = false;
			if( globalId == null )
			{
				matched = extension.getParameter("essential").valueAsBoolean();
			}
			else if( key.contains("*") )
			{
				final int star = key.indexOf("*");
				matched = globalId.startsWith(key.substring(0, star));
			}
			else
			{
				matched = globalId.equals(key);
			}

			if( matched )
			{
				for( Extension.Parameter p : extension.getParameters("dependency") )
				{
					waitFor.add(p.valueAsString());
				}
			}
		}

		String internalId = task.getInternalId();
		// Now we play the waiting game.  Nah, the waiting game sucks, let's play Hungry Hungry Hippo.
		for( String dep : waitFor )
		{
			int tries = 0;
			while( getRunningGlobalTask(dep) == null )
			{
				if( tries == 0 )
				{
					if( LOGGER.isDebugEnabled() )
					{
						LOGGER.debug("Task " + internalId + " waiting for " + dep);
					}
				}
				else if( tries == 300 )
				{
					//Two minutes should be ample
					throw new RuntimeException("Gave up waiting for tasks '" + dep + "' that " + internalId
						+ " depends on");
				}
				else if( tries % 10 == 0 )
				{
					LOGGER.warn("Task " + internalId + " STILL waiting for " + dep + " after " + tries + " seconds");
				}
				try
				{
					Thread.sleep(1000);
				}
				catch( InterruptedException in )
				{
					// Whateva
				}
				tries++;
			}
		}
	}

	protected ThreadPoolExecutor createTaskExecutor()
	{
		final int max = Math.max(maxConcurrentTasks, 8);
		final ThreadPoolExecutor tpe = new ThreadPoolExecutor(max, max, 60L, TimeUnit.SECONDS,
			new LinkedBlockingQueue<Runnable>(), new NamedThreadFactory("TaskRunner.pool"));
		tpe.allowCoreThreadTimeOut(true);
		return tpe;
	}

	protected ThreadPoolExecutor createPriorityTaskExecutor()
	{
		final ThreadPoolExecutor priorityTpe = new ThreadPoolExecutor(12, 12, 60L, TimeUnit.SECONDS,
			new LinkedBlockingQueue<Runnable>(), new NamedThreadFactory("TaskRunner.priorityPool"));
		priorityTpe.allowCoreThreadTimeOut(true);
		return priorityTpe;
	}

	protected PluginService getPluginService()
	{
		return pluginService;
	}
}
