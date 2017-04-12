package com.tle.web.loggedinusers;

import com.tle.common.security.SettingsTarget;
import com.tle.core.guice.Bind;
import com.tle.core.settings.security.AbstractSettingsPrivilegeTreeProvider;
import com.tle.web.resources.ResourcesService;

@Bind
public class LoggedInUsersPrivilegeTreeProvider extends AbstractSettingsPrivilegeTreeProvider
{
	public LoggedInUsersPrivilegeTreeProvider()
	{
		super(Type.SYSTEM_SETTING, ResourcesService.getResourceHelper(LoggedInUsersPrivilegeTreeProvider.class).key(
			"securitytree.loggedinusers"), new SettingsTarget("loggedinusers"));
	}
}
