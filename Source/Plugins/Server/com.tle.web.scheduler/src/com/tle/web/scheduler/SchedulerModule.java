package com.tle.web.scheduler;

import com.google.inject.name.Names;
import com.tle.core.guice.Bind;
import com.tle.web.sections.equella.guice.SectionsModule;

@Bind
@SuppressWarnings("nls")
public class SchedulerModule extends SectionsModule
{
	@Override
	protected void configure()
	{
		bind(Object.class).annotatedWith(Names.named("/access/settings/scheduledtasks")).toProvider(
			node(RootScheduledTasksSettingsSection.class));

		bind(Object.class).annotatedWith(Names.named("/access/scheduledtasksdebug")).toProvider(
			node(ScheduledTasksDebug.class));
	}
}
