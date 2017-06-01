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

package com.tle.web.favourites.searches;

import com.tle.core.favourites.bean.FavouriteSearch;
import com.tle.core.guice.Bind;
import com.tle.web.itemlist.item.AbstractListEntry;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.standard.model.HtmlLinkState;

@Bind
public class FavouriteSearchEntry extends AbstractListEntry
{

	private SearchFavouritesList searchFavouritesList;
	private FavouriteSearch search;

	@Override
	public HtmlLinkState getTitle()
	{
		final long searchId = search.getId();
		return new HtmlLinkState(new TextLabel(search.getName()), new OverrideHandler(
			searchFavouritesList.getRunSearchFunc(), searchId));
	}

	@Override
	public Label getDescription()
	{
		return null;
	}

	public void setSearchFavouritesList(SearchFavouritesList searchFavouritesList)
	{
		this.searchFavouritesList = searchFavouritesList;
	}

	public void setSearch(FavouriteSearch search)
	{
		this.search = search;
	}

}
