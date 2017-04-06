package com.tle.web.pss.guice;

import com.google.inject.name.Names;
import com.tle.web.pss.settings.PearsonScormServicesSettingsEditorSection;
import com.tle.web.sections.equella.guice.SectionsModule;

@SuppressWarnings("nls")
public class ScormModule extends SectionsModule
{
	@Override
	protected void configure()
	{
		bind(Object.class).annotatedWith(Names.named("/access/psssettings")).toProvider(
			node(PearsonScormServicesSettingsEditorSection.class));
	}
}
