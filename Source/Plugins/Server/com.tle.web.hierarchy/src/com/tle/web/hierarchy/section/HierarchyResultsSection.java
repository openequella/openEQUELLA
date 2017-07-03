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

package com.tle.web.hierarchy.section;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.apache.log4j.Logger;

import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.core.freetext.service.FreeTextService;
import com.tle.core.search.MappedSearchIndexValues;
import com.tle.core.services.item.FreetextResult;
import com.tle.core.services.item.FreetextSearchResults;
import com.tle.core.services.user.UserSessionService;
import com.tle.web.integration.service.IntegrationService;
import com.tle.web.itemlist.item.StandardItemListEntry;
import com.tle.web.search.base.AbstractFreetextResultsSection;
import com.tle.web.search.base.AbstractSearchResultsSection.SearchResultsModel;
import com.tle.web.search.event.FreetextSearchEvent;
import com.tle.web.search.event.FreetextSearchResultEvent;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.search.AbstractSearchActionsSection;
import com.tle.web.sections.equella.search.AbstractSearchActionsSection.AbstractSearchActionsModel;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.render.Label;
import com.tle.web.selection.SelectionService;
import com.tle.web.selection.section.RootSelectionSection.Layout;

public class HierarchyResultsSection extends AbstractFreetextResultsSection<StandardItemListEntry, SearchResultsModel>
{
	private static final Logger LOGGER = Logger.getLogger(HierarchyResultsSection.class);

	@Inject
	private HierarchyItemList itemList;
	@Inject
	private SelectionService selectionService;
	@Inject
	private IntegrationService integrationService;
	@Inject
	private FreeTextService freeText;
	@Inject
	private UserSessionService sessionService;

	@TreeLookup
	private TopicDisplaySection topicDisplay;
	@TreeLookup
	private AbstractSearchActionsSection<? extends AbstractSearchActionsModel> searchActionsSection;

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		if( !topicDisplay.isShowingResults(context) )
		{
			searchActionsSection.disableSearch(context);
			return null;
		}
		return super.renderHtml(context);
	}

	@Override
	protected Label getDefaultResultsTitle(SectionInfo info, FreetextSearchEvent searchEvent,
		FreetextSearchResultEvent resultsEvent)
	{
		return topicDisplay.getResultsTitle(info, super.getDefaultResultsTitle(info, searchEvent, resultsEvent));
	}

	@SuppressWarnings("nls")
	@Override
	protected FreetextSearchResultEvent createResultsEvent(SectionInfo info, FreetextSearchEvent searchEvent)
	{
		try
		{
			// having done a browse search, clear any currently stored
			// search-search's indexed items from the session
			sessionService.removeAttribute(MappedSearchIndexValues.MAPPED_SEARCH_ATTR_KEY);

			int[] count = freeText.countsFromFilters(Collections.singleton(searchEvent.getUnfilteredSearch()));
			FreetextSearchResults<? extends FreetextResult> results = topicDisplay.processFreetextResults(info,
				searchEvent);

			// check for dynamic resources
			List<Item> dynamicItems = topicDisplay.getDynamicKeyResourceItems(info);

			int keyResourcesSize = results.getKeyResourcesSize() + dynamicItems.size();
			int available = count[0] + keyResourcesSize;
			// Only if we're not in a skinny session shall we map the index
			// numbers, and hence enable the prev/next buttons.
			boolean inSkinny = selectionService.getCurrentSession(info) != null
				&& selectionService.getCurrentSession(info).getLayout() == Layout.SKINNY;
			if( !inSkinny )
			{
				// double check
				inSkinny = integrationService.isInIntegrationSession(info);
			}
			if( !inSkinny )
			{
				int offset = results.getOffset();
				// add in the fixed key resources
				List<Item> keyResourceItems = topicDisplay.getModel(info).getTopic().getKeyResources();
				List<ItemId> keyResourceItemIds = null;
				if( keyResourceItems.size() > 0 )
				{
					keyResourceItemIds = new ArrayList<ItemId>();
					for( Item keyItem : keyResourceItems )
					{
						keyResourceItemIds.add(keyItem.getItemId());
					}
					for( Item dynaItem : dynamicItems )
					{
						keyResourceItemIds.add(dynaItem.getItemId());
					}
				}

				MappedSearchIndexValues indexMap = new MappedSearchIndexValues(available, offset, keyResourcesSize);
				indexMap.setKeyResourceItemIds(keyResourceItemIds);
				sessionService.setAttribute(MappedSearchIndexValues.MAPPED_SEARCH_ATTR_KEY, indexMap);
				for( int i = 0; i < results.getCount(); ++i )
				{
					Item item = results.getItem(i);
					mapItemIdToSearchIndexForSession(results, searchEvent.getFinalSearch(), item, i);
				}
			}
			return new FreetextSearchResultEvent(results, searchEvent, count[0] - results.getAvailable());
		}
		catch( Exception t )
		{
			LOGGER.error("Error searching", t);
			return new FreetextSearchResultEvent(t, searchEvent);
		}
	}

	@Override
	public FreetextSearchEvent createSearchEvent(SectionInfo info)
	{
		return topicDisplay.createFreetextSearchEvent(info);
	}

	@Override
	public void processResults(SectionInfo info, FreetextSearchResultEvent event)
	{
		topicDisplay.processResults(info, event, itemList);
	}

	@Override
	protected void registerItemList(SectionTree tree, String id)
	{
		tree.registerInnerSection(itemList, id);
	}

	@Override
	public HierarchyItemList getItemList(SectionInfo info)
	{
		return itemList;
	}
}
