/*
 * Created on 16/03/2006
 */
package com.tle.web.remoting.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.core.guice.Bind;
import com.tle.core.remoting.RemoteLoginService;
import com.tle.core.services.user.UserService;
import com.tle.core.services.user.UserSessionService;
import com.tle.core.user.CurrentUser;
import com.tle.core.user.WebAuthenticationDetails;

@Bind
@Singleton
public class LoginServiceImpl implements RemoteLoginService
{
	@Inject
	private UserService userService;
	@Inject
	private UserSessionService sessionService;

	private WebAuthenticationDetails getDetails()
	{
		return userService.getWebAuthenticationDetails(RemoteInterceptor.getRequest());
	}

	@Override
	public void login(String username, String password)
	{
		userService.login(username, password, getDetails(), true);
	}

	@Override
	public void loginWithToken(String token)
	{
		userService.loginWithToken(token, getDetails(), true);
	}

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
