package com.tle.web.remoterepo.srw;

import com.tle.web.search.guice.AbstractSearchModule;

@SuppressWarnings("nls")
public class SRWModule extends AbstractSearchModule
{
	@Override
	protected NodeProvider getRootNode()
	{
		return node(SrwRootRemoteRepoSection.class);
	}

	@Override
	protected NodeProvider getQueryNode()
	{
		return node(SrwQuerySection.class);
	}

	@Override
	protected NodeProvider getResultsNode()
	{
		return node(SrwResultsSection.class);
	}

	@Override
	protected String getTreeName()
	{
		return "srwTree";
	}
}
