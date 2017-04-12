package com.tle.web.customdateformat;

import com.google.inject.name.Names;
import com.tle.web.sections.equella.guice.SectionsModule;

@SuppressWarnings("nls")
public class DateFormatSettingsModule extends SectionsModule
{
	@Override
	protected void configure()
	{
		bind(Object.class).annotatedWith(Names.named("/access/dateformatsettings")).toProvider(
			node(DateFormatSettingsSection.class));
	}
}
