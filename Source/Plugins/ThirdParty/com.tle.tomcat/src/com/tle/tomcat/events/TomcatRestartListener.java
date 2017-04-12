package com.tle.tomcat.events;

import com.tle.core.events.listeners.ApplicationListener;

public interface TomcatRestartListener extends ApplicationListener
{
	void restartTomcat();
}
