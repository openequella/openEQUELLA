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

package com.tle.web.notification.section;

import java.util.List;

import com.google.inject.Inject;
import com.tle.common.search.DefaultSearch;
import com.tle.core.notification.standard.indexer.NotificationSearch;
import com.tle.web.bulk.section.AbstractBulkSelectionSection;
import com.tle.web.notification.NotificationItemListEntry;
import com.tle.web.search.base.AbstractFreetextResultsSection;
import com.tle.web.search.base.AbstractSearchResultsSection;
import com.tle.web.search.event.FreetextSearchEvent;
import com.tle.web.search.event.FreetextSearchResultEvent;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.render.Label;

public class NotificationResultsSection
	extends
		AbstractFreetextResultsSection<NotificationItemListEntry, AbstractSearchResultsSection.SearchResultsModel>
{
	@PlugKey("noresults.items")
	private static Label LABEL_NOAVAILABLE;
	@PlugKey("noresults.items.filtered")
	private static Label LABEL_NORESULTS;

	@Inject
	private NotificationItemList itemList;

	@Override
	protected void registerItemList(SectionTree tree, String id)
	{
		tree.registerInnerSection(itemList, id);
	}

	@Override
	public NotificationItemList getItemList(SectionInfo info)
	{
		return itemList;
	}

	@Override
	protected DefaultSearch createDefaultSearch(SectionInfo info)
	{
		return new NotificationSearch();
	}

	@Override
	protected Label getNoResultsTitle(SectionInfo info, FreetextSearchEvent searchEvent,
		FreetextSearchResultEvent resultsEvent)
	{
		if( !searchEvent.isFiltered() )
		{
			return LABEL_NOAVAILABLE;
		}
		return LABEL_NORESULTS;
	}

	@Override
	protected void addAjaxUpdateDivs(SectionTree tree, List<String> ajaxList)
	{
		super.addAjaxUpdateDivs(tree, ajaxList);
		ajaxList.add(AbstractBulkSelectionSection.DIVID_SELECTBOX);
	}

}
