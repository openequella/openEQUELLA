package com.tle.core.tasks;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.core.guice.Bind;
import com.tle.core.scheduler.ScheduledTask;
import com.tle.core.services.StagingService;

/**
 * @author Nicholas Read
 */
@Bind
@Singleton
public class RemoveStagingAreas implements ScheduledTask
{
	@Inject
	private StagingService stagingService;

	@Override
	public void execute()
	{
		stagingService.removeUnusedStagingAreas();
	}
}
