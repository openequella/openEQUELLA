package com.tle.web.settings.guice;

import com.google.inject.name.Names;
import com.tle.web.sections.equella.guice.SectionsModule;
import com.tle.web.settings.section.SettingsSection;

@SuppressWarnings("nls")
public class SettingsModule extends SectionsModule
{
	@Override
	protected void configure()
	{
		bind(Object.class).annotatedWith(Names.named("settingsTree")).toProvider(node(SettingsSection.class));
	}
}
