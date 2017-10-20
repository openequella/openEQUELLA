package com.tle.core.cluster.guice;

import com.google.inject.AbstractModule;
import com.tle.core.cluster.ClusterMessageHandler;
import com.tle.core.config.guice.OptionalConfigModule;
import com.tle.core.guice.PluginTrackerModule;

@SuppressWarnings("nls")
public class ClusterModule extends AbstractModule
{
	@Override
	protected void configure()
	{
		install(new ClusterOptionalModule());
		install(new ClusterTrackerModule());
	}

	public static class ClusterOptionalModule extends OptionalConfigModule
	{
		@Override
		protected void configure()
		{
			bindProp("messaging.bindAddress");
			bindInt("messaging.bindPort", 8999);
			bindBoolean("messaging.useHostname");
		}
	}

	public static class ClusterTrackerModule extends PluginTrackerModule
	{
		@Override
		protected void configure()
		{
			bindTracker(ClusterMessageHandler.class, "clusterMessageHandler", "bean");
		}
	}
}
