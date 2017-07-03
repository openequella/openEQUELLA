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
