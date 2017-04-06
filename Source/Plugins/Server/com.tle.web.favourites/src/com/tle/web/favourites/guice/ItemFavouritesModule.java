package com.tle.web.favourites.guice;

import com.tle.web.favourites.FavouritesResultsSection;
import com.tle.web.favourites.RootFavouritesSection;
import com.tle.web.favourites.sort.FavouritesSortOptionsSection;
import com.tle.web.search.filter.FilterByAutoCompleteKeywordSection;
import com.tle.web.search.filter.FilterByDateRangeSection;
import com.tle.web.search.filter.FilterByMimeTypeSection;
import com.tle.web.selection.section.SelectionSummarySection;

@SuppressWarnings("nls")
public class ItemFavouritesModule extends AbstractFavouritesModule
{
	@Override
	protected void addSearchResultsActions(NodeProvider node)
	{
		node.child(FavouritesSortOptionsSection.class);
		node.child(FilterByAutoCompleteKeywordSection.class);
		node.child(FilterByDateRangeSection.class);
		node.child(FilterByMimeTypeSection.class);
	}

	@Override
	protected void addActions(NodeProvider node)
	{
		node.child(SelectionSummarySection.class);
	}

	@Override
	protected NodeProvider getResultsNode()
	{
		return node(FavouritesResultsSection.class);
	}

	@Override
	protected String getPrefix()
	{
		return "i";
	}

	@Override
	protected String getTreeName()
	{
		return RootFavouritesSection.ITEM_TREE_NAME;
	}
}
