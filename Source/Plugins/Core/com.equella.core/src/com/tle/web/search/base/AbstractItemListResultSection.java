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

package com.tle.web.search.base;

import javax.inject.Inject;

import com.tle.web.itemlist.item.StandardItemList;
import com.tle.web.itemlist.item.StandardItemListEntry;
import com.tle.web.search.base.AbstractSearchResultsSection.SearchResultsModel;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;

public abstract class AbstractItemListResultSection<M extends SearchResultsModel>
	extends
		AbstractFreetextResultsSection<StandardItemListEntry, M>
{
	@Inject
	private StandardItemList itemList;

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
}
