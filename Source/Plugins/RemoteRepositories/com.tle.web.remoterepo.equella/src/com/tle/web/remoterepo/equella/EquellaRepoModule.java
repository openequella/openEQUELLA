package com.tle.web.remoterepo.equella;

import com.google.inject.name.Names;
import com.tle.web.sections.equella.guice.SectionsModule;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
public class EquellaRepoModule extends SectionsModule
{
	@Override
	protected void configure()
	{
		bind(Object.class).annotatedWith(Names.named("equellaTree")).toProvider(equellaTree());
	}

	private NodeProvider equellaTree()
	{
		NodeProvider node = node(EquellaRootRemoteRepoSection.class);
		node.child(EquellaRepoStartSection.class);
		// node.innerChild(EquellaRepoDownloadProgressSection.class);
		return node;
	}
}
