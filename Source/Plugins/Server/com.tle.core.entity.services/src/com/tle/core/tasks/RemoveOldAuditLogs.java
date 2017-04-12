package com.tle.core.tasks;

import javax.inject.Named;
import javax.inject.Singleton;

import com.google.inject.Inject;
import com.tle.core.auditlog.AuditLogService;
import com.tle.core.guice.Bind;
import com.tle.core.scheduler.ScheduledTask;

/**
 * @author Nicholas Read
 */
@Bind
@Singleton
public class RemoveOldAuditLogs implements ScheduledTask
{
	@Inject
	private AuditLogService auditLogService;
	@Inject(optional = true)
	@Named("com.tle.core.tasks.RemoveOldAuditLogs.daysBeforeRemoval")
	// Increased from 7 and can be overrode by the optional-config.properties
	private int daysBeforeRemoval = 120;

	@Override
	public void execute()
	{
		auditLogService.removeOldLogs(daysBeforeRemoval);
	}
}
