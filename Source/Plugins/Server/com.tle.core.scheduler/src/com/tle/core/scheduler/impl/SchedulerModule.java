package com.tle.core.scheduler.impl;

import com.tle.core.config.guice.OptionalConfigModule;

public class SchedulerModule extends OptionalConfigModule
{
	@SuppressWarnings("nls")
	@Override
	protected void configure()
	{
		bindInt("com.tle.core.tasks.RemoveDeletedItems.daysBeforeRemoval");
		bindInt("com.tle.core.tasks.RemoveOldAuditLogs.daysBeforeRemoval");
	}
}
