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

package com.tle.web.browseby;

import javax.inject.Inject;

import com.tle.common.search.DefaultSearch;
import com.tle.web.itemlist.item.StandardItemList;
import com.tle.web.itemlist.item.StandardItemListEntry;
import com.tle.web.search.base.AbstractFreetextResultsSection;
import com.tle.web.search.base.AbstractSearchResultsSection.SearchResultsModel;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.events.RenderEventContext;

public class BrowseSearchResults extends AbstractFreetextResultsSection<StandardItemListEntry, SearchResultsModel>
{
	@Inject
	private StandardItemList itemList;
	@TreeLookup
	private BrowseSection browseSection;

	@Override
	public StandardItemList getItemList(SectionInfo info)
	{
		return itemList;
	}

	@Override
	protected void registerItemList(SectionTree tree, String id)
	{
		tree.registerInnerSection(itemList, id);
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		if( !browseSection.isSearching(context) )
		{
			return null;
		}
		return super.renderHtml(context);
	}

	@Override
	protected DefaultSearch createDefaultSearch(SectionInfo info)
	{
		return new BrowseSearch();
	}
}
