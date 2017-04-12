package com.tle.web.externaltools.guice;

import com.google.inject.name.Names;
import com.tle.web.externaltools.section.ExternalToolContributeSection;
import com.tle.web.externaltools.section.RootExternalToolsSection;
import com.tle.web.externaltools.section.ShowExternalToolsSection;
import com.tle.web.sections.equella.guice.SectionsModule;

@SuppressWarnings("nls")
public class ExternalToolsModule extends SectionsModule
{

	@Override
	protected void configure()
	{
		bind(Object.class).annotatedWith(Names.named("externalToolsTree")).toProvider(externalToolsTree());
	}

	private NodeProvider externalToolsTree()
	{
		NodeProvider node = node(RootExternalToolsSection.class);
		node.innerChild(ExternalToolContributeSection.class);
		node.child(ShowExternalToolsSection.class);
		return node;
	}
}

