package com.tle.core.workflow.daily;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.inject.Provider;
import com.tle.core.guice.Bind;
import com.tle.core.scheduler.ScheduledTask;
import com.tle.core.services.item.ItemService;

/**
 * @author Nicholas Read
 */
@Bind
@Singleton
public class CheckReviewTask implements ScheduledTask
{
	@Inject
	private ItemService itemService;
	@Inject
	private Provider<ReviewFilter> reviewFactory;

	@Override
	public void execute()
	{
		itemService.operateAll(reviewFactory.get());
	}
}
