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

package com.tle.web.cloud.search.section;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import com.tle.annotation.NonNullByDefault;
import com.tle.common.search.DefaultSearch;
import com.tle.core.cloud.beans.converted.CloudItem;
import com.tle.core.cloud.service.CloudSearchResults;
import com.tle.core.cloud.service.CloudService;
import com.tle.core.guice.Bind;
import com.tle.web.cloud.event.CloudSearchEvent;
import com.tle.web.cloud.event.CloudSearchResultsEvent;
import com.tle.web.cloud.search.CloudSearchListEntry;
import com.tle.web.itemlist.item.ListSettings;
import com.tle.web.search.base.AbstractSearchResultsSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;

/**
 * @author Aaron
 */
@NonNullByDefault
@Bind
public class CloudSearchResultsSection
	extends
		AbstractSearchResultsSection<CloudSearchListEntry, CloudSearchEvent, CloudSearchResultsEvent, CloudSearchResultsSection.CloudSearchResultsModel>
{
	@Inject
	private CloudService cloudService;

	@Inject
	private CloudSearchItemListSection list;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		tree.registerInnerSection(list, id);
	}

	@Override
	public void processResults(SectionInfo info, CloudSearchResultsEvent event)
	{
		final CloudSearchResults results = event.getResults();
		final List<CloudItem> searchResults = results.getResults();
		for( CloudItem cloudItemBean : searchResults )
		{
			list.addItem(info, cloudItemBean, null);
		}

		final Collection<String> words = new DefaultSearch.QueryParser(event.getEvent().getQuery()).getHilightedList();
		final ListSettings<CloudSearchListEntry> listSettings = list.getListSettings(info);
		listSettings.setHilightedWords(words);
	}

	@Override
	protected CloudSearchResultsEvent createResultsEvent(SectionInfo info, CloudSearchEvent searchEvent)
	{
		CloudSearchResults results = cloudService.search(searchEvent.getCloudSearch(), searchEvent.getOffset(),
			searchEvent.getCount());

		CloudSearchResultsEvent resultsEvent = new CloudSearchResultsEvent(searchEvent, results,
			results.getFilteredOut());

		return resultsEvent;
	}

	@Override
	public CloudSearchEvent createSearchEvent(SectionInfo info)
	{
		CloudSearchEvent event = new CloudSearchEvent(null);
		return event;
	}

	@Override
	public CloudSearchItemListSection getItemList(SectionInfo info)
	{
		return list;
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new CloudSearchResultsModel();
	}

	public static class CloudSearchResultsModel extends AbstractSearchResultsSection.SearchResultsModel
	{
		// Here be dragons
	}
}
