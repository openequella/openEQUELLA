/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
		install(new UserManagementTrackerModule());
	}

	public static class UserManagementTrackerModule extends PluginTrackerModule
	{
		@Override
		protected String getPluginId()
		{
			return "com.tle.core.usermanagement";
		}

		@Override
		protected void configure()
		{
			bindTracker(UserDirectory.class, "userManager", null).setIdParam("settingsClass").orderByParameter("order",
				true);
			bindTracker(UserManagementLogonFilter.class, "logonFilter", "bean").orderByParameter("order");
		}
	}

}
