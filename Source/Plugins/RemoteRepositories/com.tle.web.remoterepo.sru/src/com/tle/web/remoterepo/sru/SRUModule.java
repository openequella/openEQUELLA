/**
 * 
 */
package com.tle.web.remoterepo.sru;

import com.tle.web.search.guice.AbstractSearchModule;

@SuppressWarnings("nls")
public class SRUModule extends AbstractSearchModule
{
	private static String SRU_TREE = "sruTree";

	@Override
	protected NodeProvider getRootNode()
	{
		return node(SruRootRemoteRepoSection.class);
	}

	@Override
	protected NodeProvider getQueryNode()
	{
		return node(SruQuerySection.class);
	}

	@Override
	protected NodeProvider getResultsNode()
	{
		return node(SruResultsSection.class);
	}

	@Override
	protected String getTreeName()
	{
		return SRU_TREE;
	}
}
