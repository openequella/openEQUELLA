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
