package com.tle.web.remoterepo.z3950;

import com.tle.web.search.guice.AbstractSearchModule;

@SuppressWarnings("nls")
public class Z3950Module extends AbstractSearchModule
{
	@Override
	protected NodeProvider getRootNode()
	{
		return node(Z3950RootRemoteRepoSection.class);
	}

	@Override
	protected NodeProvider getQueryNode()
	{
		return node(Z3950QuerySection.class);
	}

	@Override
	protected NodeProvider getResultsNode()
	{
		return node(Z3950ResultsSection.class);
	}

	@Override
	protected NodeProvider getPagingNode()
	{
		return node(Z3950PagingSection.class);
	}

	@Override
	protected String getTreeName()
	{
		return "z3950Tree";
	}
}
