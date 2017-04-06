package com.tle.web.cloud.guice;

import com.tle.web.search.actions.StandardFavouriteSearchSection;

@SuppressWarnings("nls")
public class CloudCourseSearchModule extends CloudSearchModule
{
	@Override
	protected String getTreeName()
	{
		return "/access/course/cloudsearch";
	}

	@Override
	protected void addSearchResultsActions(NodeProvider node)
	{
		super.addSearchResultsActions(node);
		node.child(StandardFavouriteSearchSection.class);
	}

	@Override
	protected void addActions(NodeProvider node)
	{
		// Do not.
	}
}
