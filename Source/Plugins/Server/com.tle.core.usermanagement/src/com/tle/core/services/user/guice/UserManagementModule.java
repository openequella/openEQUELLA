package com.tle.core.services.user.guice;

import com.tle.core.config.guice.OptionalConfigModule;
import com.tle.core.guice.PluginTrackerModule;
import com.tle.plugins.ump.UserDirectory;
import com.tle.plugins.ump.UserManagementLogonFilter;

@SuppressWarnings("nls")
public class UserManagementModule extends OptionalConfigModule
{
	@Override
	protected void configure()
	{
		bindBoolean("userService.useXForwardedFor");
		bindBoolean("userSessionService.noSessionTracking");
		install(new UserManagementTrackerModule());
	}

	public static class UserManagementTrackerModule extends PluginTrackerModule
	{
		@Override
		protected void configure()
		{
			bindTracker(UserDirectory.class, "userManager", null).setIdParam("settingsClass").orderByParameter("order",
				true);
			bindTracker(UserManagementLogonFilter.class, "logonFilter", "bean").orderByParameter("order");
		}
	}

}
