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

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.log4j.MDC;

import com.dytech.edge.common.Constants;
import com.dytech.edge.exceptions.WebException;
import com.dytech.edge.web.WebConstants;
import com.google.common.base.Throwables;
import com.tle.core.guice.Bind;
import com.tle.core.plugins.PluginService;
import com.tle.core.plugins.PluginTracker;
import com.tle.core.plugins.PluginTracker.ExtensionParamComparator;
import com.tle.core.services.user.UserService;
import com.tle.core.services.user.UserSessionService;
import com.tle.common.usermanagement.user.AnonymousUserState;
import com.tle.common.institution.CurrentInstitution;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.common.usermanagement.user.UserState;
import com.tle.web.core.filter.UserStateResult.Result;
import com.tle.web.dispatcher.AbstractWebFilter;
import com.tle.web.dispatcher.FilterResult;
import com.tle.web.dispatcher.WebFilterCallback;

/**
 * Sets up the TLE session.
 * 
 * @author Nicholas Read
 */
@Bind
@Singleton
public class TleSessionFilter extends AbstractWebFilter
{
	private static final Logger LOGGER = Logger.getLogger(TleSessionFilter.class);

	@Inject
	private UserSessionService sessionService;
	@Inject
	private UserService userService;
	private PluginTracker<UserStateHook> userStateHooks;

	@Override
	public FilterResult filterRequest(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		final FilterResult result = new FilterResult(new WebFilterCallback()
		{
			@Override
			public void afterServlet(HttpServletRequest request, HttpServletResponse response)
			{
				sessionService.unbind();
				CurrentUser.setUserState(null);
			}
		});
		try
		{
			sessionService.bindRequest(request);
			final UserState existingUserState = sessionService.getAttribute(WebConstants.KEY_USERSTATE);
			if( existingUserState != null )
			{
				userService.useUser(existingUserState);
				sessionService.nudgeSession();
			}
			final UserStateResult userResult = getUserState(request, existingUserState);
			Result resultType = userResult.getResult();
			if( resultType == Result.EXISTING && existingUserState == null )
			{
				resultType = Result.GUEST;
			}
			switch( resultType )
			{
				case GUEST:
					if( existingUserState == null || !existingUserState.isGuest() )
					{
						userService.logoutToGuest(userService.getWebAuthenticationDetails(request), true);
					}
					break;
				case LOGIN:
				case LOGIN_SESSION:
					userService.login(userResult.getUserState(), resultType == Result.LOGIN_SESSION);
					break;
				case EXISTING:
					break;
			}
			UserState userState = CurrentUser.getUserState();
			if( userState.getSessionID() == null )
			{
				LOGGER.warn("Session ID for current user is null: " + userState.getUserBean().getUsername()); //$NON-NLS-1$
			}
			else
			{
				MDC.put(Constants.MDC_SESSION_ID, userState.getSessionID());
			}
		}
		catch( Exception t )
		{
			userService.logoutToGuest(userService.getWebAuthenticationDetails(request), false);
			throw Throwables.propagate(t);
		}

		return result;
	}

	private UserStateResult getUserState(HttpServletRequest request, UserState userState) throws WebException
	{
		// stolen from EPS - allow OAuth token to set system user for
		// Institution manipulation
		for( UserStateHook hook : userStateHooks.getBeanList() )
		{
			if( !hook.isInstitutional() )
			{
				UserStateResult result = hook.getUserState(request, userState);
				if( result != null )
				{
					return result;
				}
			}
		}
		if( CurrentInstitution.get() == null )
		{
			if( userState != null )
			{
				return new UserStateResult(Result.EXISTING);
			}
			AnonymousUserState guest = new AnonymousUserState();
			guest.setSessionID("guest"); //$NON-NLS-1$
			return new UserStateResult(guest);
		}

		for( UserStateHook hook : userStateHooks.getBeanList() )
		{
			UserStateResult result = hook.getUserState(request, userState);
			if( result != null )
			{
				return result;
			}
		}

		// They're a guest then
		if( userState == null )
		{
			return new UserStateResult(Result.GUEST);
		}
		return new UserStateResult(Result.EXISTING);
	}

	@SuppressWarnings("nls")
	@Inject
	public void setPluginService(PluginService pluginService)
	{
		userStateHooks = new PluginTracker<UserStateHook>(pluginService, "com.tle.web.core", "userStateHook", "id",
			new ExtensionParamComparator("order"));
		userStateHooks.setBeanKey("bean");
	}
}
