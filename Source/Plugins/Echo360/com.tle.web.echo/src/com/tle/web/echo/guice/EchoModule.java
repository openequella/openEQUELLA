package com.tle.web.echo.guice;

import com.google.inject.name.Names;
import com.tle.web.echo.section.EchoServerEditorSection;
import com.tle.web.echo.section.EchoServerListSection;
import com.tle.web.echo.section.RootEchoServerSection;
import com.tle.web.sections.equella.guice.SectionsModule;

@SuppressWarnings("nls")
public class EchoModule extends SectionsModule
{
	@Override
	protected void configure()
	{
		bind(Object.class).annotatedWith(Names.named("/access/echoservers")).toProvider(echoTree());
	}

	private NodeProvider echoTree()
	{
		NodeProvider node = node(RootEchoServerSection.class);
		node.innerChild(EchoServerEditorSection.class);
		node.child(EchoServerListSection.class);
		return node;
	}
}
