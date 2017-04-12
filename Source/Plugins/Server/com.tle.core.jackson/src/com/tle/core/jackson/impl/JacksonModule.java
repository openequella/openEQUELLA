package com.tle.core.jackson.impl;

import com.tle.core.guice.PluginTrackerModule;
import com.tle.core.jackson.MapperExtension;

@SuppressWarnings("nls")
public class JacksonModule extends PluginTrackerModule
{
	@Override
	protected void configure()
	{
		bindTracker(MapperExtension.class, "mapperExtension", "bean");
	}
}
