package com.tle.web.diagnostics.guice;

import com.google.inject.name.Names;
import com.tle.web.diagnostics.section.DiagnosticsUsersListSection;
import com.tle.web.sections.equella.guice.SectionsModule;


@SuppressWarnings("nls")
public class DiagnosticsUsersModule extends SectionsModule
{
	@Override
	protected void configure()
	{
		bind(Object.class).annotatedWith(Names.named("/access/diagnostics")).toProvider(
			node(DiagnosticsUsersListSection.class));
	}
}