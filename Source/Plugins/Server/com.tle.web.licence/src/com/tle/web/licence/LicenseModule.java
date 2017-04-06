package com.tle.web.licence;

import com.google.inject.name.Names;
import com.tle.web.sections.equella.guice.SectionsModule;

public class LicenseModule extends SectionsModule
{

	@SuppressWarnings("nls")
	@Override
	protected void configure()
	{
		bind(Object.class).annotatedWith(Names.named("/licenceSectionDisplay")).toProvider(node(LicenceSection.class));
	}

}
