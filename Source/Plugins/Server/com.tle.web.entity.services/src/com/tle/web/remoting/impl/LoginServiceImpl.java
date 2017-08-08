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

package com.tle.web.remoting.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.core.guice.Bind;
import com.tle.core.remoting.RemoteLoginService;
import com.tle.core.services.user.UserService;
import com.tle.core.services.user.UserSessionService;

@Bind
@Singleton
public class LoginServiceImpl implements RemoteLoginService
{
	@Inject
	private UserService userService;
	@Inject
	private UserSessionService sessionService;

	@Override
	public String getLoggedInUserId()
	{
		return CurrentUser.getUserID();
	}

	@Override
	public void keepAlive()
	{
		userService.keepAlive();
	}

	@Override
	@SuppressWarnings("nls")
	public void logout()
	{
		sessionService.setAttribute("$LOGOUT$", Boolean.TRUE);
	}
}
