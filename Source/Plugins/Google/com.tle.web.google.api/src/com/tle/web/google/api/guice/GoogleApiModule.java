package com.tle.web.google.api.guice;

import com.google.inject.name.Names;
import com.tle.web.google.api.settings.GoogleApiSettingsEditorSection;
import com.tle.web.sections.equella.guice.SectionsModule;

public class GoogleApiModule extends SectionsModule
{
	@Override
	protected void configure()
	{
		bind(Object.class).annotatedWith(Names.named("/access/googleapisettings")).toProvider(
			node(GoogleApiSettingsEditorSection.class));
	}
}
