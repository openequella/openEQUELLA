package com.tle.core.scheduler.standard.guice;

import com.tle.core.config.guice.OptionalConfigModule;

/**
 * @author Aaron
 *
 */
public class SchedulerStandardModule extends OptionalConfigModule
{
	@Override
	protected void configure()
	{
		bindProp("com.tle.core.tasks.RemoveOldAuditLogs.daysBeforeRemoval");
	}
}
