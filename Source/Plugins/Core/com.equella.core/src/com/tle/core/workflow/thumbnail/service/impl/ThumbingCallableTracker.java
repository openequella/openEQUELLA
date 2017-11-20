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

package com.tle.core.workflow.thumbnail.service.impl;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.log4j.Logger;

import com.google.inject.assistedinject.Assisted;
import com.tle.beans.Institution;
import com.tle.beans.item.ItemId;
import com.tle.common.BlockingThreadPoolExecutor;
import com.tle.common.NamedThreadFactory;
import com.tle.core.guice.Bind;
import com.tle.core.guice.BindFactory;
import com.tle.core.institution.RunAsInstitution;
import com.tle.core.workflow.thumbnail.service.ThumbnailRequestService;

/**
 * @author Aaron
 *
 */
@SuppressWarnings("nls")
@Bind
@Singleton
/* package protected */class ThumbingCallableTracker
{
	private static final Logger LOGGER = Logger.getLogger(ThumbingCallableTracker.class);

	@Inject
	private ThumbnailRequestService thumbnailRequestService;
	@Inject
	private ThumbingCallableFactory callableFactory;
	@Inject
	private RunAsInstitution runAs;

	private CompletionService<ThumbingCallableResult> completionService;

	@PostConstruct
	public void init()
	{
		final ThreadPoolExecutor executor = new BlockingThreadPoolExecutor(2, 2, 5, TimeUnit.MINUTES, 2,
			TimeUnit.MINUTES, new NamedThreadFactory("ThumbnailServiceExecutor"), new Callable<Boolean>()
			{
				@Override
				public Boolean call()
				{
					//Wait forever
					LOGGER.trace("Waited 2 minutes to queue a thumb job, waiting again.");
					return true;
				}
			});
		completionService = new ExecutorCompletionService<ThumbingCallableResult>(executor);

		new Thread()
		{
			@Override
			public void run()
			{
				setName("Thumb task finisher listener");
				watchCompleted();
			}
		}.start();
	}

	public Future<ThumbingCallableResult> submitTask(Institution institution, String requestUuid, ItemId itemId,
		String serialHandle)
	{
		final ThumbingCallable callable = callableFactory.getRunnable(institution, requestUuid, itemId, serialHandle);
		final Future<ThumbingCallableResult> future = completionService.submit(callable);
		return future;
	}

	public void watchCompleted()
	{
		while( true )
		{
			try
			{
				final Future<ThumbingCallableResult> future = completionService.take();
				String thumbnailRequestUuid = null;
				Institution institution = null;
				try
				{
					final ThumbingCallableResult taskInfo = future.get();
					institution = taskInfo.getInstitution();
					thumbnailRequestUuid = taskInfo.getRequestUuid();
				}
				catch( CancellationException cancelled )
				{
					LOGGER.debug("Thread cancelled");
				}
				catch( ExecutionException e )
				{
					LOGGER.error("Error waiting for task completion", e);
				}
				finally
				{
					if( thumbnailRequestUuid != null && institution != null )
					{
						final String reqUuid = thumbnailRequestUuid;
						runAs.executeAsSystem(institution, new Callable<Void>()
						{
							@Override
							public Void call() throws Exception
							{
								thumbnailRequestService.delete(reqUuid);
								return null;
							}
						});
					}
				}
			}
			catch( InterruptedException e )
			{
				LOGGER.warn("Task finish thread interrupted, ignoring");
			}
			catch( Throwable t )
			{
				LOGGER.warn("Task finish thread got unknown error, restarting", t);
			}
		}
	}

	@BindFactory
	public interface ThumbingCallableFactory
	{
		ThumbingCallable getRunnable(@Assisted Institution institution, @Assisted("requestUuid") String requestUuid,
			@Assisted ItemId itemId, @Assisted("serialHandle") String serialHandle);
	}
}
