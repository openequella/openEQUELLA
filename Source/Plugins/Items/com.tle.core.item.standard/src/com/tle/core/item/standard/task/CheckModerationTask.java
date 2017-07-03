package com.tle.core.item.standard.task;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.inject.Provider;
import com.tle.core.guice.Bind;
import com.tle.core.item.service.ItemService;
import com.tle.core.item.standard.filter.workflow.CheckModerationFilter;
import com.tle.core.scheduler.ScheduledTask;

/**
 * @author Nicholas Read
 */
@Bind
@Singleton
public class CheckModerationTask implements ScheduledTask
{
	@Inject
	private ItemService itemService;
	@Inject
	private Provider<CheckModerationFilter> filterFactory;

	@Override
	public void execute()
	{
		itemService.operateAll(filterFactory.get());
	}

}
