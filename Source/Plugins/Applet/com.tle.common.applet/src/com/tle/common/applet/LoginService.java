/*
 * Created on 29/11/2005
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
