package com.tle.web.remotecaching;

import com.google.inject.name.Names;
import com.tle.web.sections.equella.guice.SectionsModule;

/**
 * @author larry
 */
public class RemoteCachingModule extends SectionsModule
{
	@Override
	protected void configure()
	{
		bind(Object.class).annotatedWith(Names.named("/access/remotecaching")).toProvider(
			node(RootRemoteCachingSection.class));
	}
}
