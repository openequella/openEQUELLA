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

package com.tle.core.item.standard.task;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.springframework.transaction.annotation.Transactional;

import com.tle.core.guice.Bind;
import com.tle.core.item.service.ItemService;
import com.tle.core.item.standard.FilterFactory;
import com.tle.core.notification.standard.service.NotificationPreferencesService;
import com.tle.core.scheduler.ScheduledTask;

/**
 * @author Nicholas Read
 */
@Bind
@Singleton
public class NotifyOfNewItemsTask implements ScheduledTask
{
	@Inject
	private NotificationPreferencesService notificationPrefs;
	@Inject
	private ItemService itemService;
	@Inject
	private FilterFactory filterFactory;

	@Transactional
	@Override
	public void execute()
	{
		itemService.operateAll(filterFactory.createFilter(notificationPrefs.getWatchedCollectionMap()));
	}

}
