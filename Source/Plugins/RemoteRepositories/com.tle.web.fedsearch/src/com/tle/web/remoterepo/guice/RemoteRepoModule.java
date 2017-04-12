package com.tle.web.remoterepo.guice;

import com.google.inject.name.Names;
import com.tle.web.remoterepo.section.RemoteRepoListAllSection;
import com.tle.web.sections.equella.guice.SectionsModule;

public class RemoteRepoModule extends SectionsModule
{
	@SuppressWarnings("nls")
	@Override
	protected void configure()
	{
		bind(Object.class).annotatedWith(Names.named("/access/remoterepo")).toProvider(
			node(RemoteRepoListAllSection.class));
	}
}