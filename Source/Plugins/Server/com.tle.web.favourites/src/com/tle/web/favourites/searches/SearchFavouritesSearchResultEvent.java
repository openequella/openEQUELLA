package com.tle.web.favourites.searches;

import com.tle.common.searching.SearchResults;
import com.tle.core.favourites.bean.FavouriteSearch;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.search.event.AbstractSearchResultsEvent;
import com.tle.web.sections.equella.search.event.SearchResultsListener;

public class SearchFavouritesSearchResultEvent extends AbstractSearchResultsEvent<SearchFavouritesSearchResultEvent>
{
	private final SearchResults<FavouriteSearch> results;
	private final int filteredOut;

	public SearchFavouritesSearchResultEvent(SearchResults<FavouriteSearch> results, int filteredOut)
	{
		this.results = results;
		this.filteredOut = filteredOut;
	}

	@Override
	public int getCount()
	{
		return results.getCount();
	}

	@Override
	public int getMaximumResults()
	{
		return results.getAvailable();
	}

	@Override
	public int getOffset()
	{
		return results.getOffset();
	}

	@Override
	public void fire(SectionId sectionId, SectionInfo info,
		SearchResultsListener<SearchFavouritesSearchResultEvent> listener) throws Exception
	{
		listener.processResults(info, this);
	}

	public SearchResults<FavouriteSearch> getResults()
	{
		return results;
	}

	@Override
	public int getFilteredOut()
	{
		return filteredOut;
	}
}
