package com.tle.core.events.guice;

import com.google.inject.AbstractModule;
import com.tle.core.events.EventExecutor;
import com.tle.core.events.listeners.ApplicationListener;
import com.tle.core.guice.PluginTrackerModule;

/**
 * @author Aaron
 *
 */
public class EventsModule extends AbstractModule
{
	@Override
	protected void configure()
	{
		install(new EventsTrackerModule());
	}

	public static class EventsTrackerModule extends PluginTrackerModule
	{
		@Override
		protected void configure()
		{
			bindTracker(ApplicationListener.class, "applicationEventListener", null);
			bindTracker(EventExecutor.class, "eventExecutor", "bean");
		}
	}
}
