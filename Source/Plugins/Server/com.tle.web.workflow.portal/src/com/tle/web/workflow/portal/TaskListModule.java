package com.tle.web.workflow.portal;

import com.tle.core.guice.PluginTrackerModule;

public class TaskListModule extends PluginTrackerModule
{
	@SuppressWarnings("nls")
	@Override
	protected void configure()
	{
		bindTracker(TaskListExtension.class, "filter", "bean").orderByParameter("order");
	}
}
