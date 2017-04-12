package com.tle.web.google.analytics;

import com.google.inject.name.Names;
import com.tle.web.sections.equella.guice.SectionsModule;

public class GoogleAnalyticsModule extends SectionsModule
{
	@SuppressWarnings("nls")
	@Override
	protected void configure()
	{
		bind(Object.class).annotatedWith(Names.named("/access/googleAnalyticsPage.do")).toProvider(
			node(GoogleAnalyticsPage.class));
	}

}
