package com.tle.core.metadata.guice;

import com.tle.core.guice.PluginTrackerModule;
import com.tle.core.metadata.MetadataHandler;

public class MetadataHandlerTrackerModule extends PluginTrackerModule
{
	@SuppressWarnings("nls")
	@Override
	protected void configure()
	{
		bindTracker(MetadataHandler.class, "metadataHandlers", "bean").orderByParameter("order");
	}
}
