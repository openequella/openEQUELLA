package com.tle.web.remoting.rest.docs;

import com.google.inject.name.Names;
import com.tle.web.sections.equella.guice.SectionsModule;

public class DocsModule extends SectionsModule
{

	@Override
	protected void configure()
	{
		bind(Object.class).annotatedWith(Names.named("/docsSection")).toProvider( //$NON-NLS-1$
			node(DocsSection.class));
	}

}
