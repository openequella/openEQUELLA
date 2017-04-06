package com.tle.core.services.item.relation;

import com.tle.core.guice.PluginTrackerModule;
import com.tle.core.services.guice.ServicesModule;

@SuppressWarnings("nls")
public class RelationModule extends ServicesModule
{

	@Override
	protected void configure()
	{
		super.configure();
		requestStaticInjection(RelationModify.class);
		install(new RelationTrackerModule());
	}

	public static class RelationTrackerModule extends PluginTrackerModule
	{
		@Override
		protected void configure()
		{
			bindTracker(RelationListener.class, "relationListener", "bean");
		}
	}
}
