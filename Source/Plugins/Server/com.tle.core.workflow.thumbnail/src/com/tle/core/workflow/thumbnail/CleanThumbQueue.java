package com.tle.core.workflow.thumbnail;

import javax.inject.Singleton;

import com.google.inject.Inject;
import com.tle.core.guice.Bind;
import com.tle.core.scheduler.ScheduledTask;
import com.tle.core.workflow.thumbnail.service.ThumbnailRequestService;

@Bind
@Singleton
public class CleanThumbQueue implements ScheduledTask
{
	@Inject
	ThumbnailRequestService thumb;

	@Override
	public void execute()
	{
		thumb.cleanThumbQueue();
	}
}
