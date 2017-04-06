package com.tle.core.tasks;

import javax.inject.Named;
import javax.inject.Singleton;

import com.google.inject.Inject;
import com.tle.core.guice.Bind;
import com.tle.core.scheduler.ScheduledTask;
import com.tle.core.services.item.ItemService;
import com.tle.core.workflow.filters.FilterFactory;

/**
 * @author Nicholas Read
 */
@Bind
@Singleton
public class RemoveDeletedItems implements ScheduledTask
{
	@Inject
	private ItemService itemService;
	@Inject
	private FilterFactory filterFactory;
	@Inject(optional = true)
	// can be overrode by the optional-config.properties
	@Named("com.tle.core.tasks.RemoveDeletedItems.daysBeforeRemoval")
	private int daysBeforeRemoval = 7;

	@Override
	public void execute()
	{
		itemService.operateAll(filterFactory.removeDeleted(daysBeforeRemoval));
	}
}
