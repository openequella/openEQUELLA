/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
