package com.tle.web.oaiidentifier;

import com.google.inject.name.Names;
import com.tle.web.sections.equella.guice.SectionsModule;

public class OaiIdentifierSettingsModule extends SectionsModule
{
	@Override
	protected void configure()
	{
		bind(Object.class).annotatedWith(Names.named("/access/oaiidentifiersettings")).toProvider(
			node(RootOaiIdentifierSettingsSection.class));
	}
}
