package com.tle.core.scheduler.standard.task;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.core.filesystem.staging.service.StagingService;
import com.tle.core.guice.Bind;
import com.tle.core.scheduler.ScheduledTask;

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
