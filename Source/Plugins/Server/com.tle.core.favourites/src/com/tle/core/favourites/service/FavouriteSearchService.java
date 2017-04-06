package com.tle.core.favourites.service;

import java.util.List;

import com.tle.common.searching.Search;
import com.tle.core.favourites.SearchFavouritesSearchResults;
import com.tle.core.favourites.bean.FavouriteSearch;
import com.tle.web.sections.SectionInfo;

public interface FavouriteSearchService
{
	void save(FavouriteSearch search);

	SearchFavouritesSearchResults search(Search search, int offset, int perPage);

	void deleteById(long id);

	void executeSearch(SectionInfo info, long id);

	List<FavouriteSearch> getSearchesForOwner(String userID, int maxResults);
}
