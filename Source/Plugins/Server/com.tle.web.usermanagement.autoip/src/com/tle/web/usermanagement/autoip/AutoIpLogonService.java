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

package com.tle.web.usermanagement.autoip;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import com.google.inject.Singleton;
import com.tle.beans.system.AutoLogin;
import com.tle.common.Check;
import com.tle.core.guice.Bind;
import com.tle.core.services.config.ConfigurationService;
import com.tle.core.services.user.UserService;
import com.tle.core.user.UserState;
import com.tle.core.user.WebAuthenticationDetails;

@Bind
@Singleton
public class AutoIpLogonService
{
	@Inject
	private ConfigurationService configService;
	@Inject
	private UserService userService;

	public boolean autoLogon(HttpServletRequest request)
	{
		AutoLogin settings = userService.getAttribute(AutoLogin.class);
		WebAuthenticationDetails details = userService.getWebAuthenticationDetails(request);
		if( configService.isAutoLoginAvailable(settings, details.getIpAddress()) )
		{
			String autoLoginAsUsername = settings.getUsername();
			if( !Check.isEmpty(autoLoginAsUsername) )
			{
				// We want to catch any errors, otherwise we get a great big
				// stack trace
				UserState userState = userService.authenticateAsUser(autoLoginAsUsername, details);
				userState.setWasAutoLoggedIn(true);
				userService.login(userState, false);
				return true;
			}
		}
		return false;
	}
}
