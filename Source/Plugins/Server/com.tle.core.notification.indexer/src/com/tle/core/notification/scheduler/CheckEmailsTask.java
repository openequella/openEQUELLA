package com.tle.core.notification.scheduler;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.core.guice.Bind;
import com.tle.core.notification.NotificationService;
import com.tle.core.scheduler.ScheduledTask;
import com.tle.core.services.TaskService;

@Bind
@Singleton
public class CheckEmailsTask implements ScheduledTask
{
	@Inject
	private TaskService taskService;
	@Inject
	private NotificationService notificationService;

	@Override
	public void execute()
	{
		taskService.getGlobalTask(notificationService.getClusteredTask(true), TimeUnit.MINUTES.toMillis(1));
	}
}
