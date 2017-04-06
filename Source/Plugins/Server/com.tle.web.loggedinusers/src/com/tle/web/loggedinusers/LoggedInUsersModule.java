package com.tle.web.loggedinusers;

import com.google.inject.name.Names;
import com.tle.web.sections.equella.guice.SectionsModule;

@SuppressWarnings("nls")
public class LoggedInUsersModule extends SectionsModule
{
	@Override
	protected void configure()
	{
		bind(Object.class).annotatedWith(Names.named("/access/liu")).toProvider(node(LoggedInUsersListSection.class));
	}
}