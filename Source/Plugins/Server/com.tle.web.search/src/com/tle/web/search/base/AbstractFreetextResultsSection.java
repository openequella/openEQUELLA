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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.apache.log4j.Logger;

import com.dytech.edge.exceptions.InvalidSearchQueryException;
import com.tle.beans.item.Item;
import com.tle.common.search.DefaultSearch;
import com.tle.common.search.LiveItemSearch;
import com.tle.common.search.PresetSearch;
import com.tle.core.freetext.service.FreeTextService;
import com.tle.core.search.MappedSearchIndexValues;
import com.tle.core.services.item.FreetextResult;
import com.tle.core.services.item.FreetextSearchResults;
import com.tle.core.services.user.UserSessionService;
import com.tle.web.integration.service.IntegrationService;
import com.tle.web.itemlist.item.AbstractItemList;
import com.tle.web.itemlist.item.AbstractItemListEntry;
import com.tle.web.itemlist.item.ListSettings;
import com.tle.web.search.base.AbstractSearchResultsSection.SearchResultsModel;
import com.tle.web.search.event.FreetextSearchEvent;
import com.tle.web.search.event.FreetextSearchResultEvent;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.TreeIndexed;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.selection.section.RootSelectionSection.Layout;

@TreeIndexed
public abstract class AbstractFreetextResultsSection<LE extends AbstractItemListEntry, M extends SearchResultsModel>
	extends
		AbstractSearchResultsSection<LE, FreetextSearchEvent, FreetextSearchResultEvent, M>
{
	private static final Logger LOGGER = Logger.getLogger(AbstractFreetextResultsSection.class);

	@PlugKey("search.invalidfreetext")
	private static Label LABEL_INVALIDFREETEXT;

	@Inject
	private IntegrationService integrationService;
	@Inject
	private FreeTextService freeText;
	@Inject
	private UserSessionService sessionService;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		registerItemList(tree, id);
	}

	protected abstract void registerItemList(SectionTree tree, String id);

	@Override
	public abstract AbstractItemList<LE, ?> getItemList(SectionInfo info);

	@Override
	public void processResults(SectionInfo info, FreetextSearchResultEvent event)
	{
		FreetextSearchResults<? extends FreetextResult> results = event.getResults();
		FreetextSearchEvent searchEvent = event.getSearchEvent();

		DefaultSearch finalSearch = searchEvent.getFinalSearch();
		Collection<String> words = new DefaultSearch.QueryParser(searchEvent.getSearchedText()).getHilightedList();

		if( finalSearch instanceof PresetSearch )
		{
			PresetSearch presetSearch = (PresetSearch) finalSearch;
			boolean dynamicCollection = presetSearch.isDynamicCollection();
			String queryText = presetSearch.getQueryText();
			if( dynamicCollection )
			{
				// remove dynamic collection filter freetext search word from
				// the highlighted list
				words.remove(queryText);
			}
		}

		AbstractItemList<LE, ?> itemList = getItemList(info);
		ListSettings<LE> settings = itemList.getListSettings(info);
		customiseSettings(info, settings);
		settings.setHilightedWords(words);
		int count = results.getCount();
		boolean flagListAsNullItemsRemoved = false;

		// Ensure the NullItemsRemoved flag is cleared: may have been set in a
		// previous iteration
		itemList.setNullItemsRemovedOnModel(info, false);

		// On having conducted any search, expunge the mapped-index structure
		// from any previous search that may be held by the session.
		sessionService.removeAttribute(MappedSearchIndexValues.MAPPED_SEARCH_ATTR_KEY);

		// The existence of either Layout.SKINNY or affirmative to
		// isInIntegrationSession is enough to flag that we avoid mapping index
		// values for the benefit of next/prev buttons
		boolean inSkinny = false;
		if( count > 0 )
		{
			inSkinny = getSelectionService().getCurrentSession(info) != null
				&& getSelectionService().getCurrentSession(info).getLayout() == Layout.SKINNY;
			if( !inSkinny )
			{
				inSkinny = integrationService.isInIntegrationSession(info);
			}
		}
		// The existence of null items in the results list can only occur if an
		// item has been purged in the very recent (ie within the last few
		// seconds) past, and yet the indexer hasn't caught up. More likely to
		// occur in automated tests than in reality. Nonetheless, if we find a
		// null item, flag its existence.
		for( int i = 0; i < count; i++ )
		{
			Item item = results.getItem(i);
			if( item != null )
			{
				itemList.addItem(info, item, results.getResultData(i));
				// Only if we're not in a skinny session shall we map the
				// index numbers, and hence enable the prev/next buttons.
				if( !inSkinny )
				{
					mapItemIdToSearchIndexForSession(results, event.getSearchEvent().getFinalSearch(), item, i);
				}
			}
			else
			{
				flagListAsNullItemsRemoved = true;
			}
		}

		// nullItemsRemoved flag set as a property of the model (so as to be
		// accessible by the ftl)
		if( flagListAsNullItemsRemoved )
		{
			itemList.setNullItemsRemovedOnModel(info, true);
		}
	}

	/**
	 * The object placed into the session is basically a map of ItemId, Integer,
	 * whereby the integer is the index value into the search result set. In
	 * addition to the map however, we also need to preserve the total available
	 * size (ie above and beyond the limits to the returned page size) from the
	 * search. Accordingly the object lodged with the session is a Pair<Map,
	 * Integer>.
	 * 
	 * @param results
	 * @param item
	 * @param i
	 */
	protected void mapItemIdToSearchIndexForSession(FreetextSearchResults<? extends FreetextResult> results,
		DefaultSearch search, Item item, int i)
	{
		MappedSearchIndexValues indexMap = null;
		Object mapAttr = sessionService.getAttribute(MappedSearchIndexValues.MAPPED_SEARCH_ATTR_KEY);
		if( mapAttr != null )
		{
			indexMap = (MappedSearchIndexValues) mapAttr;
		}
		else
		{
			int available = results.getAvailable();
			int offset = results.getOffset();
			indexMap = new MappedSearchIndexValues(available, offset, 0);
			indexMap.setActiveSearch(search);
			sessionService.setAttribute(MappedSearchIndexValues.MAPPED_SEARCH_ATTR_KEY, indexMap);
		}
		indexMap.mapItemIdWithIndex(item.getItemId(), i, true);
	}

	protected void customiseSettings(SectionInfo info, ListSettings<LE> settings)
	{
		// for subclasses
	}

	@Override
	protected List<Label> getErrorMessageLabels(SectionInfo info, FreetextSearchEvent searchEvent,
		FreetextSearchResultEvent resultsEvent)
	{
		Throwable exception = resultsEvent.getException();
		List<Label> errorLabels = new ArrayList<Label>();
		if( exception instanceof InvalidSearchQueryException )
		{
			errorLabels.add(LABEL_INVALIDFREETEXT);
		}
		else
		{
			errorLabels.add(new TextLabel(exception.getMessage()));
		}

		return errorLabels;
	}

	@Override
	public FreetextSearchEvent createSearchEvent(SectionInfo info)
	{
		DefaultSearch[] searches = createSearches(info);
		String priv = getSelectionService().getSearchPrivilege(info);
		searches[0].setPrivilege(priv);
		searches[1].setPrivilege(priv);
		return new FreetextSearchEvent(searches[0], searches[1]);
	}

	protected DefaultSearch[] createSearches(SectionInfo info)
	{
		return new DefaultSearch[]{createDefaultSearch(info), createDefaultSearch(info)};
	}

	protected DefaultSearch createDefaultSearch(SectionInfo info)
	{
		return new LiveItemSearch();
	}

	@Override
	protected FreetextSearchResultEvent createResultsEvent(SectionInfo info, FreetextSearchEvent searchEvent)
	{
		try
		{
			if( searchEvent.getException() != null )
			{
				throw searchEvent.getException();
			}
			int[] count = freeText.countsFromFilters(Collections.singleton(searchEvent.getUnfilteredSearch()));
			FreetextSearchResults<FreetextResult> results = freeText.search(searchEvent.getFinalSearch(),
				searchEvent.getOffset(), searchEvent.getCount());
			return new FreetextSearchResultEvent(results, searchEvent, count[0] - results.getAvailable());
		}
		catch( Exception t )
		{
			LOGGER.error("Error searching", t); //$NON-NLS-1$
			return new FreetextSearchResultEvent(t, searchEvent);
		}
	}

}