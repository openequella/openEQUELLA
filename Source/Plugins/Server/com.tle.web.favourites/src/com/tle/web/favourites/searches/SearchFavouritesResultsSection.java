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

import javax.inject.Inject;

import com.tle.common.search.DefaultSearch;
import com.tle.core.favourites.SearchFavouritesSearchResults;
import com.tle.core.favourites.bean.FavouriteSearch;
import com.tle.core.favourites.service.FavouriteSearchService;
import com.tle.core.guice.Bind;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.search.base.AbstractSearchResultsSection;
import com.tle.web.search.base.AbstractSearchResultsSection.SearchResultsModel;
import com.tle.web.search.event.FreetextSearchEvent;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.search.AbstractSearchActionsSection;
import com.tle.web.sections.equella.search.AbstractSearchActionsSection.AbstractSearchActionsModel;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.render.Label;
import com.tle.web.template.section.event.BlueBarEvent;
import com.tle.web.template.section.event.BlueBarEventListener;

@Bind
public class SearchFavouritesResultsSection
	extends
		AbstractSearchResultsSection<FavouriteSearchEntry, FreetextSearchEvent, SearchFavouritesSearchResultEvent, SearchResultsModel>
	implements
		BlueBarEventListener
{
	@PlugKey("noresults.searches")
	private static Label LABEL_NOAVAILABLE;
	@PlugKey("noresults.searches.filtered")
	private static Label LABEL_NORESULTS;

	@Inject
	private FavouriteSearchService favSearchService;
	@Inject
	private SearchFavouritesList searchFavouritesList;

	@ViewFactory
	private FreemarkerFactory viewFactory;

	@TreeLookup
	private AbstractSearchActionsSection<? extends AbstractSearchActionsModel> searchActionsSection;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		tree.registerInnerSection(searchFavouritesList, id);
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		searchActionsSection.disableSaveAndShare(context);
		return super.renderHtml(context);
	}

	@Override
	public void processResults(SectionInfo info, SearchFavouritesSearchResultEvent results)
	{
		for( FavouriteSearch favsearch : results.getResults().getResults() )
		{
			searchFavouritesList.addSearch(info, favsearch);
		}
	}

	@Override
	public FreetextSearchEvent createSearchEvent(SectionInfo info)
	{
		return new FreetextSearchEvent(new DefaultSearch(), new DefaultSearch());
	}

	@Override
	protected SearchFavouritesSearchResultEvent createResultsEvent(SectionInfo info, FreetextSearchEvent searchEvent)
	{
		SearchFavouritesSearchResults results = favSearchService.search(searchEvent.getFinalSearch(),
			searchEvent.getOffset(), searchEvent.getCount());

		SearchFavouritesSearchResults unfiltered = favSearchService.search(searchEvent.getUnfilteredSearch(),
			searchEvent.getOffset(), searchEvent.getCount());

		return new SearchFavouritesSearchResultEvent(results, unfiltered.getAvailable() - results.getAvailable());
	}

	@Override
	protected Label getNoResultsTitle(SectionInfo info, FreetextSearchEvent searchEvent,
		SearchFavouritesSearchResultEvent resultsEvent)
	{
		if( !searchEvent.isFiltered() )
		{
			return LABEL_NOAVAILABLE;
		}
		return LABEL_NORESULTS;
	}

	@Override
	public void addBlueBarResults(RenderContext context, BlueBarEvent event)
	{
		event.addHelp(viewFactory.createResult("helpfavouritessearches.ftl", this)); //$NON-NLS-1$
	}

	@Override
	public SearchFavouritesList getItemList(SectionInfo info)
	{
		return searchFavouritesList;
	}
}
