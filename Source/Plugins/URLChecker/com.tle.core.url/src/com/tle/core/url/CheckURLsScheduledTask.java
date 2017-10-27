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

package com.tle.core.url;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import com.google.common.base.Optional;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.inject.Singleton;
import com.tle.beans.Institution;
import com.tle.beans.ReferencedURL;
import com.tle.core.dao.helpers.BatchingIterator;
import com.tle.core.guice.Bind;
import com.tle.core.institution.InstitutionService;
import com.tle.core.scheduler.ScheduledTask;
import com.tle.core.events.services.EventService;
import com.tle.core.services.HttpService;
import com.tle.core.system.service.SchemaDataSourceService;
import com.tle.core.url.URLEvent.URLEventType;
import com.tle.core.url.dao.URLCheckerDao;

/**
 * @author Nicholas Read
 */
@Bind
@Singleton
public class CheckURLsScheduledTask implements ScheduledTask
{
	private static final int MAX_CONCURRENT_CHECKS = 200;

	@Inject
	private SchemaDataSourceService schemaDataSourceService;
	@Inject
	private URLCheckerDao dao;
	@Inject
	private URLCheckerService service;
	@Inject
	private EventService eventService;
	@Inject
	private InstitutionService institutionService;
	@Inject
	private HttpService httpService;
	@Inject
	private URLCheckerPolicy policy;

	@Override
	public void execute()
	{
		if( !httpService.canAccessInternet() )
		{
			// Nothing we can do...
			return;
		}

		Multimap<Long, Institution> schemaToInsts = institutionService.getAvailableMap();
		for( final Map.Entry<Long, Collection<Institution>> entry : schemaToInsts.asMap().entrySet() )
		{
			schemaDataSourceService.executeWithSchema(entry.getKey(), new Callable<Object>()
			{
				@Override
				public Object call() throws Exception
				{
					executeForInstitutions(entry.getValue());
					return null;
				}
			});
		}
	}

	private void executeForInstitutions(Collection<Institution> insts) throws InterruptedException, ExecutionException
	{
		Iterator<ReferencedURL> bi = new ReferencedURLIterator();

		// Asynchronously check a maximum number of URLs at any given time. As
		// they complete, remove them from the working set and fill it back up
		// with more.
		Set<ListenableFuture<ReferencedURL>> workingset = Sets.newHashSetWithExpectedSize(MAX_CONCURRENT_CHECKS);
		while( bi.hasNext() || !workingset.isEmpty() )
		{
			// Remove completed URLs.
			for( Iterator<ListenableFuture<ReferencedURL>> iter = workingset.iterator(); iter.hasNext(); )
			{
				ListenableFuture<ReferencedURL> f = iter.next();
				if( f.isDone() )
				{
					iter.remove();

					ReferencedURL rurl = f.get();
					dao.updateWithTransaction(rurl);

					// Send event if exactly at the warning or disabled level.
					URLEventType eventType = null;
					if( rurl.getTries() == policy.getTriesUntilWarning() )
					{
						eventType = URLEventType.URL_WARNING;
					}
					else if( rurl.getTries() == policy.getTriesUntilDisabled() )
					{
						eventType = URLEventType.URL_DISABLED;
					}

					if( eventType != null )
					{
						// Send event to all institutions
						eventService.publishApplicationEvent(insts, new URLEvent(eventType, rurl.getUrl()));
					}
				}
			}

			// Fill up the working set with maximum allowed.
			while( bi.hasNext() && workingset.size() < MAX_CONCURRENT_CHECKS )
			{
				workingset.add(service.checkUrl(bi.next()));
			}

			// Now sleep for a couple of seconds before trying again, at least
			// some URLs should already be complete.
			Thread.sleep(TimeUnit.SECONDS.toMillis(2));
		}
	}

	private class ReferencedURLIterator extends BatchingIterator<ReferencedURL>
	{
		private static final int BATCH_SIZE = 200;

		@Override
		protected Iterator<ReferencedURL> getMore(Optional<ReferencedURL> lastObj)
		{
			long startId = lastObj.isPresent() ? lastObj.get().getId() + 1 : 1;
			return dao.getRecheckingBatch(startId, BATCH_SIZE).iterator();
		}
	}
}
