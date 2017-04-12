package com.tle.web.favourites.guice;

import com.tle.web.favourites.RootFavouritesSection;
import com.tle.web.favourites.searches.SearchFavouritesResultsSection;
import com.tle.web.favourites.searches.sort.SearchFavouritesSortOptionsSection;
import com.tle.web.search.filter.FilterByDateRangeSection;
import com.tle.web.search.filter.FilterByKeywordSection;

@SuppressWarnings("nls")
public class SearchFavouritesModule extends AbstractFavouritesModule
{
	@Override
	protected void addSearchResultsActions(NodeProvider node)
	{
		node.child(SearchFavouritesSortOptionsSection.class);
		node.child(FilterByKeywordSection.class);
		node.child(FilterByDateRangeSection.class);
	}

	@Override
	protected NodeProvider getResultsNode()
	{
		return node(SearchFavouritesResultsSection.class);
	}

	@Override
	protected String getPrefix()
	{
		return "s";
	}

	@Override
	protected String getTreeName()
	{
		return RootFavouritesSection.SEARCH_TREE_NAME;
	}
}
