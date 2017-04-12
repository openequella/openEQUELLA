package com.tle.web.myresources.guice;

import com.tle.web.myresources.MyResourcesFavouriteSearchAction;
import com.tle.web.myresources.MyResourcesSearchResults;
import com.tle.web.myresources.MyResourcesSearchTypeSection;
import com.tle.web.myresources.MyResourcesSortSection;
import com.tle.web.myresources.RootMyResourcesSection;
import com.tle.web.search.actions.StandardShareSearchQuerySection;
import com.tle.web.search.filter.FilterByAutoCompleteKeywordSection;
import com.tle.web.search.filter.FilterByCollectionSection;
import com.tle.web.search.filter.FilterByDateRangeSection;
import com.tle.web.search.filter.FilterByItemStatusSection;
import com.tle.web.search.filter.FilterByMimeTypeSection;
import com.tle.web.search.guice.AbstractSearchModule;

@SuppressWarnings("nls")
public class MyResourcesModule extends AbstractSearchModule
{
	@Override
	protected void addSearchResultsActions(NodeProvider node)
	{
		node.child(StandardShareSearchQuerySection.class);
		node.child(MyResourcesSortSection.class);
		node.child(FilterByAutoCompleteKeywordSection.class);
		node.child(FilterByCollectionSection.class);
		node.child(FilterByItemStatusSection.class);
		node.child(FilterByDateRangeSection.class);
		node.child(FilterByMimeTypeSection.class);
	}

	@Override
	protected NodeProvider getRootNode()
	{
		return node(RootMyResourcesSection.class);
	}

	@Override
	protected NodeProvider getQueryNode()
	{
		return node(MyResourcesSearchTypeSection.class);
	}

	@Override
	protected void addQueryActions(NodeProvider node)
	{
		node.child(MyResourcesFavouriteSearchAction.class);
	}

	@Override
	protected NodeProvider getResultsNode()
	{
		return node(MyResourcesSearchResults.class);
	}

	@Override
	protected String getTreeName()
	{
		return "/access/myresources";
	}
}
