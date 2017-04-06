package com.tle.web.remoting.resteasy;

import com.tle.core.guice.PluginTrackerModule;

@SuppressWarnings("nls")
public class RestEasyModule extends PluginTrackerModule
{
	@Override
	protected void configure()
	{
		bindTracker(Object.class, "resource", null);
	}
}
