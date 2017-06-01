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

package com.tle.common.applet;

import java.net.MalformedURLException;
import java.net.URL;

import com.tle.common.applet.client.ClientProxyFactory;
import com.tle.core.remoting.RemoteLoginService;

public class LoginService
{
	@SuppressWarnings("nls")
	public static final String LOGON_PATH = "invoker/" + RemoteLoginService.class.getName() + ".service";

	private final URL server;
	private final RemoteLoginService service;

	public LoginService(SessionHolder session)
	{
		server = session.getUrl();
		service = ClientProxyFactory.createProxy(RemoteLoginService.class, getLoginURL());
	}

	public URL getLoginURL()
	{
		try
		{
			return new URL(server, server.getPath() + LOGON_PATH);
		}
		catch( MalformedURLException e )
		{
			throw new RuntimeException(e);
		}
	}

	public void keepAlive()
	{
		service.keepAlive();
	}

	public void login(String username, String password)
	{
		service.login(username, password);
	}

	public void loginWithToken(String token)
	{
		service.loginWithToken(token);
	}

	public void logout()
	{
		service.logout();
	}

	public String getLoggedInUserId()
	{
		return service.getLoggedInUserId();
	}
}
