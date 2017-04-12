/*
 * Created on 29/11/2005
 */
package com.tle.common.applet;

import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SessionHolder
{
	static final Log LOGGER = LogFactory.getLog(SessionHolder.class);

	private final LoginService loginService;
	private final KeepAliveTask keepAliveTask;
	private final URL url;

	public SessionHolder(URL url)
	{
		this.url = url;
		loginService = new LoginService(this);
		keepAliveTask = new KeepAliveTask(this);
	}

	public void enableKeepAlive(boolean b)
	{
		if( b )
		{
			keepAliveTask.onSchedule();
		}
		else
		{
			keepAliveTask.cancel();
		}
	}

	public URL getUrl()
	{
		return url;
	}

	public LoginService getLoginService()
	{
		return loginService;
	}
}
