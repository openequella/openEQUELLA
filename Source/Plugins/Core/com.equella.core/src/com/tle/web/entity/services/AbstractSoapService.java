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

package com.tle.web.entity.services;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.xml.ws.WebServiceContext;

import org.apache.cxf.transport.http.AbstractHTTPDestination;

import com.tle.common.Check;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.common.usermanagement.user.UserState;
import com.tle.common.usermanagement.user.WebAuthenticationDetails;
import com.tle.core.services.user.UserService;

/**
 * @author jmaginnis
 */
public abstract class AbstractSoapService
{
	@Inject
	private UserService userService;
	@Inject
	private WebServiceContext webServiceContext;

	/*
	 * (non-Javadoc)
	 * @see com.tle.core.remoting.ClientService#logout()
	 */
	public void logout(String session)
	{
		authenticate(session);
		userService.logoutToGuest(getDetails(), false);
	}

	protected WebAuthenticationDetails getDetails()
	{
		return userService.getWebAuthenticationDetails(
			(HttpServletRequest) webServiceContext.getMessageContext().get(AbstractHTTPDestination.HTTP_REQUEST));
	}

	/*
	 * (non-Javadoc)
	 * @see com.tle.core.remoting.ClientService#keepAlive()
	 */
	public void keepAlive()
	{
		userService.keepAlive();
	}

	public String login(String username, String password)
	{
		return userService.login(username, password, getDetails(), true).getSessionID();
	}

	public String loginWithToken(String token)
	{
		return userService.loginWithToken(token, getDetails(), true).getSessionID();
	}

	/**
	 * client does not _need_ to pass in a session id, it's just used to verify
	 * their cookies are working properly.
	 * 
	 * @param session The current session id as returned from login() or
	 *            loginWithToken()
	 */
	protected void authenticate(String session)
	{
		if( !Check.isEmpty(session) )
		{
			UserState state = CurrentUser.getUserState();

			// they have supplied a session id which they got from login()
			// but it doesn't match the current user's session id
			// therefore user doesn't have cookies turned on. Need to tell them.
			String userSession = state.getSessionID();
			if( !userSession.equals(session) )
			{
				throw new RuntimeException(CurrentLocale.get("com.tle.web.remoting.soap.error.nocookies", userSession)); //$NON-NLS-1$
			}
		}
	}
}
