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

package com.tle.web.core.filter;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.dytech.edge.exceptions.WebException;
import com.dytech.edge.web.WebConstants;
import com.tle.common.Check;
import com.tle.core.guice.Bind;
import com.tle.core.services.user.UserService;
import com.tle.common.usermanagement.user.UserState;
import com.tle.exceptions.TokenException;
import com.tle.web.core.filter.UserStateResult.Result;

/**
 * @author Aaron
 */
@Bind
@Singleton
public class TokenUserStateHook implements UserStateHook
{
	private static final Logger LOGGER = Logger.getLogger(TokenUserStateHook.class);

	@Inject
	private UserService userService;

	@Override
	public UserStateResult getUserState(HttpServletRequest request, UserState userState) throws WebException
	{
		// We want/need token to override the current session
		String token = request.getParameter(WebConstants.TOKEN_AUTHENTICATION_PARAM);
		if( !Check.isEmpty(token) )
		{
			boolean noExistingSessionOrGuest = userState == null || userState.isGuest();
			if( noExistingSessionOrGuest || !userService.verifyUserStateForToken(userState, token) )
			{
				try
				{
					// This call may also throw a UsernameNotFound exception -
					// we do *NOT* want to catch this. UsernameNotFound
					// exception should propagate back to the user screen as an
					// error.
					UserState hookState = userService.authenticateWithToken(token,
						userService.getWebAuthenticationDetails(request));
					if( hookState != null )
					{
						return new UserStateResult(hookState);
					}
					return null;
				}
				catch( TokenException ex )
				{
					LOGGER.warn("Error with token:" + token);
					request.setAttribute(WebConstants.KEY_LOGIN_EXCEPTION, ex);

					if( !noExistingSessionOrGuest )
					{
						// We want to make sure they logout of their invalid
						// state.
						return new UserStateResult(Result.GUEST);
					}
				}
			}
		}
		return null;
	}

	@Override
	public boolean isInstitutional()
	{
		return true;
	}
}
