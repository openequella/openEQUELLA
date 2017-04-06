package com.tle.web.customlinks.guice;

import com.google.inject.name.Names;
import com.tle.web.customlinks.section.CustomLinksSection;
import com.tle.web.customlinks.section.RootCustomLinksSection;
import com.tle.web.sections.equella.guice.SectionsModule;

public class CustomLinksModule extends SectionsModule
{

	@Override
	protected void configure()
	{
		bind(Object.class).annotatedWith(Names.named("customLinksTree")).toProvider(
			node(RootCustomLinksSection.class).child(CustomLinksSection.class));

	}

}
