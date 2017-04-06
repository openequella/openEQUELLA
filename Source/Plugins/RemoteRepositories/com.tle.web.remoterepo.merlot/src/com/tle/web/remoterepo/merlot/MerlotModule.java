package com.tle.web.remoterepo.merlot;

import com.tle.web.remoterepo.merlot.filter.MerlotDateRangeFilterSection;
import com.tle.web.remoterepo.merlot.filter.MerlotFilterKeywordTypeSection;
import com.tle.web.remoterepo.merlot.filter.MerlotFilterOptionsSection;
import com.tle.web.remoterepo.merlot.sort.MerlotSortOptionsSection;
import com.tle.web.search.guice.AbstractSearchModule;

@SuppressWarnings("nls")
public class MerlotModule extends AbstractSearchModule
{
	@Override
	protected void addSearchResultsActions(NodeProvider node)
	{
		node.child(MerlotSortOptionsSection.class);
		node.child(MerlotFilterKeywordTypeSection.class);
		node.child(MerlotFilterOptionsSection.class);
		node.child(MerlotDateRangeFilterSection.class);
	}

	@Override
	protected NodeProvider getPagingNode()
	{
		return node(MerlotPagingSection.class);
	}

	@Override
	protected NodeProvider getRootNode()
	{
		return node(MerlotRootRemoteRepoSection.class);
	}

	@Override
	protected NodeProvider getQueryNode()
	{
		return node(MerlotQuerySection.class);
	}

	@Override
	protected NodeProvider getResultsNode()
	{
		return node(MerlotResultsSection.class);
	}

	@Override
	protected String getTreeName()
	{
		return "merlotTree";
	}
}
