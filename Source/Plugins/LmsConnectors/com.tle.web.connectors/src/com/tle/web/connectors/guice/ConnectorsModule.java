package com.tle.web.connectors.guice;

import com.google.inject.name.Names;
import com.tle.web.connectors.section.ConnectorContributeSection;
import com.tle.web.connectors.section.RootConnectorsSection;
import com.tle.web.connectors.section.ShowConnectorsSection;
import com.tle.web.sections.equella.guice.SectionsModule;

public class ConnectorsModule extends SectionsModule
{
	@SuppressWarnings("nls")
	@Override
	protected void configure()
	{
		bind(Object.class).annotatedWith(Names.named("connectorsTree")).toProvider(connectorsTree());
	}

	private NodeProvider connectorsTree()
	{
		NodeProvider node = node(RootConnectorsSection.class);
		node.innerChild(ConnectorContributeSection.class);
		node.child(ShowConnectorsSection.class);
		return node;
	}
}
