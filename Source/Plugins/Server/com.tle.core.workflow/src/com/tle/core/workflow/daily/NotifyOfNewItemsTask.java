package com.tle.core.workflow.daily;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.springframework.transaction.annotation.Transactional;

import com.tle.core.guice.Bind;
import com.tle.core.scheduler.ScheduledTask;
import com.tle.core.services.item.ItemService;
import com.tle.core.workflow.notification.WorkflowPreferencesService;

/**
 * @author Nicholas Read
 */
@Bind
@Singleton
public class NotifyOfNewItemsTask implements ScheduledTask
{
	@Inject
	private WorkflowPreferencesService workflowPreferencesService;
	@Inject
	private ItemService itemService;
	@Inject
	private NewItemFactory factory;

	@Transactional
	@Override
	public void execute()
	{
		itemService.operateAll(factory.createFilter(workflowPreferencesService.getWatchedCollectionMap()));
	}

}
