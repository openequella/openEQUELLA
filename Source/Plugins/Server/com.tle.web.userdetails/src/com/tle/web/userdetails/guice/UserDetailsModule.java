package com.tle.web.userdetails.guice;

import com.google.inject.name.Names;
import com.tle.web.sections.equella.guice.SectionsModule;
import com.tle.web.userdetails.EditUserSection;

public class UserDetailsModule extends SectionsModule
{

	@Override
	protected void configure()
	{
		bind(Object.class).annotatedWith(Names.named("/access/user")).toProvider(node(EditUserSection.class));
	}

}
