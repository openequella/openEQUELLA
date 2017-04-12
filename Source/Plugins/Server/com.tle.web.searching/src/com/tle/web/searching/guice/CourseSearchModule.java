package com.tle.web.searching.guice;

import com.tle.web.search.actions.StandardFavouriteSearchSection;
import com.tle.web.searching.section.IntegrationShareSearchQuerySection;

@SuppressWarnings("nls")
public class CourseSearchModule extends StandardSearchModule
{
	@Override
	protected void addSearchResultsActions(NodeProvider node)
	{
		super.addSearchResultsActions(node);
		node.child(StandardFavouriteSearchSection.class);
	}

	@Override
	protected void addActions(NodeProvider node)
	{
		// No actions
	}

	@Override
	protected NodeProvider getShareSection()
	{
		return node(IntegrationShareSearchQuerySection.class);
	}

	@Override
	protected String getTreeName()
	{
		return "/access/course/search";
	}
}
