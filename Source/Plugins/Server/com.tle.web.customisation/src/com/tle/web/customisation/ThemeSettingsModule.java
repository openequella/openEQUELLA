package com.tle.web.customisation;

import com.google.inject.name.Names;
import com.tle.core.guice.Bind;
import com.tle.web.sections.equella.guice.SectionsModule;

@Bind
public class ThemeSettingsModule extends SectionsModule
{
	@Override
	protected void configure()
	{
		bind(Object.class).annotatedWith(Names.named("/access/themesettings")).toProvider(node(RootThemeSection.class));
	}
}
