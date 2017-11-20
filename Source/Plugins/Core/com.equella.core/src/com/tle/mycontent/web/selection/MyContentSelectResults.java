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

package com.tle.mycontent.web.selection;

import java.util.Set;

import javax.inject.Inject;

import com.tle.beans.item.ItemStatus;
import com.tle.common.Check;
import com.tle.common.search.DefaultSearch;
import com.tle.mycontent.MyContentConstants;
import com.tle.web.itemlist.item.ListSettings;
import com.tle.web.itemlist.item.StandardItemListEntry;
import com.tle.web.myresources.MyResourcesSearch;
import com.tle.web.search.base.AbstractItemListResultSection;
import com.tle.web.search.base.AbstractSearchResultsSection.SearchResultsModel;
import com.tle.web.sections.SectionInfo;
import com.tle.web.selection.filter.SelectionAllowedMimeTypes;

public class MyContentSelectResults extends AbstractItemListResultSection<SearchResultsModel>
{
	@Inject
	private SelectionAllowedHandlers allowedHandlers;
	@Inject
	private SelectionAllowedMimeTypes allowedMimeTypes;

	@Override
	protected DefaultSearch createDefaultSearch(SectionInfo info)
	{
		MyResourcesSearch search = new MyResourcesSearch();
		search.setItemStatuses(ItemStatus.PERSONAL);
		Set<String> handlers = allowedHandlers.get(info);
		if( !Check.isEmpty(handlers) )
		{
			search.addMust('/' + MyContentConstants.CONTENT_TYPE_NODE, handlers);
		}
		search.setMimeTypes(allowedMimeTypes.get(info));
		return search;
	}

	@Override
	protected void customiseSettings(SectionInfo info, ListSettings<StandardItemListEntry> settings)
	{
		settings.setEditable(false);
	}
}
