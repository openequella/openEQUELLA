package com.tle.core.favourites;

import java.util.List;

import com.tle.common.searching.SimpleSearchResults;
import com.tle.core.favourites.bean.FavouriteSearch;

public class SearchFavouritesSearchResults extends SimpleSearchResults<FavouriteSearch>
{
	private static final long serialVersionUID = 1L;

	public SearchFavouritesSearchResults(List<FavouriteSearch> results, int count, int offset, int available)
	{
		super(results, count, offset, available);
	}
}
