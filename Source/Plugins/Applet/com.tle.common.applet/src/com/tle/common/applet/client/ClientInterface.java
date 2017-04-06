/*
 * Created on 4/11/2005
 */
package com.tle.common.applet.client;

import com.tle.common.applet.KeepAliveTask;
import com.tle.common.applet.TimeoutHandler;

public interface ClientInterface
{
	KeepAliveTask getKeepAliveTask();

	TimeoutHandler getTimeoutHandler();

	String getSession();
}
