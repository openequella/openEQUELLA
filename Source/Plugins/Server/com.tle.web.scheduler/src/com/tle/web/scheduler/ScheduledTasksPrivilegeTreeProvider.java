package com.tle.web.scheduler;

import com.tle.common.security.SettingsTarget;
import com.tle.core.guice.Bind;
import com.tle.core.settings.security.AbstractSettingsPrivilegeTreeProvider;
import com.tle.web.resources.ResourcesService;

@Bind
@SuppressWarnings("nls")
public class ScheduledTasksPrivilegeTreeProvider extends AbstractSettingsPrivilegeTreeProvider
{
	public ScheduledTasksPrivilegeTreeProvider()
	{
		super(Type.SYSTEM_SETTING, ResourcesService.getResourceHelper(ScheduledTasksSettingsSection.class).key(
			"securitytree.scheduler"), new SettingsTarget("scheduledTasks"));
	}
}
