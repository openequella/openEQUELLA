package com.tle.web.sections.convert;

import com.tle.core.guice.PluginTrackerModule;

public class ConverterTrackerModule extends PluginTrackerModule
{
	@SuppressWarnings("nls")
	@Override
	protected void configure()
	{
		bind(Conversion.class).asEagerSingleton();
		bindTracker(SectionsConverter.class, "converter", null);
	}
}
