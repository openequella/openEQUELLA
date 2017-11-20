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

package com.tle.web.searching.section;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import com.tle.common.search.DefaultSearch;
import com.tle.common.settings.standard.SearchSettings;
import com.tle.core.settings.service.ConfigurationService;
import com.tle.web.itemlist.item.AbstractItemList;
import com.tle.web.itemlist.item.StandardItemListEntry;
import com.tle.web.search.base.AbstractFreetextResultsSection;
import com.tle.web.search.base.AbstractSearchResultsSection.SearchResultsModel;
import com.tle.web.search.base.ContextableSearchSection;
import com.tle.web.search.event.FreetextSearchEvent;
import com.tle.web.search.event.FreetextSearchResultEvent;
import com.tle.web.searching.GallerySearchResults;
import com.tle.web.searching.StandardSearchResultType;
import com.tle.web.searching.StandardSearchResults;
import com.tle.web.searching.VideoSearchResults;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.utils.KeyOption;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.DynamicHtmlListModel;

public final class SearchResultsSection
	extends
		AbstractFreetextResultsSection<StandardItemListEntry, SearchResultsModel>
{
	@PlugKey("searching.search.results")
	private static Label LABEL_SEARCHRESULTS;

	@TreeLookup
	private SearchQuerySection querySection;

	@EventFactory
	private EventGenerator events;

	@Inject
	private GallerySearchResults galleryResults;
	@Inject
	private StandardSearchResults standardResults;
	@Inject
	private ConfigurationService configService;
	@Inject
	private VideoSearchResults videoResults;
	@Component(parameter = "type", supported = true, contexts = ContextableSearchSection.HISTORYURL_CONTEXT)
	private SingleSelectionList<StandardSearchResultType> resultType;

	@Override
	public AbstractItemList<StandardItemListEntry, ?> getItemList(SectionInfo info)
	{
		return resultType.getSelectedValue(info).getCustomItemList();
	}

	@Override
	protected Label getDefaultResultsTitle(SectionInfo info, FreetextSearchEvent searchEvent,
		FreetextSearchResultEvent resultsEvent)
	{
		return LABEL_SEARCHRESULTS;
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		StandardSearchResultType resType = resultType.getSelectedValue(context);
		Set<String> matchingValues = resultType.getListModel().getMatchingValues(context,
			Collections.singleton(resType.getValue()));
		if( matchingValues.size() == 0 )
		{
			String defaultValue = resultType.getListModel().getDefaultValue(context);
			resultType.setSelectedStringValue(context, defaultValue);
		}

		boolean gallery = resType.equals(galleryResults) && !resType.isDisabled();
		getPaging().setIsGalleryOptions(context, gallery);

		boolean videoGallery = resType.equals(videoResults) && !resType.isDisabled();
		getPaging().setIsVideoGallery(context, videoGallery);
		return super.renderHtml(context);
	}

	@Override
	protected DefaultSearch[] createSearches(SectionInfo info)
	{
		StandardSearchResultType selectedResults = resultType.getSelectedValue(info);
		DefaultSearch search = querySection.createDefaultSearch(info, true);
		DefaultSearch unfiltered = querySection.createDefaultSearch(info, true);
		if( resultType.getSelectedValue(info).isDisabled() )
		{
			resultType.setSelectedValue(info, standardResults);
		}
		else
		{
			selectedResults.addResultTypeDefaultRestrictions(search);
		}

		selectedResults.addResultTypeDefaultRestrictions(unfiltered);

		return new DefaultSearch[]{search, unfiltered};
	}

	@Override
	protected SingleSelectionList<StandardSearchResultType> getResultTypeSelector(SectionInfo info)
	{
		return resultType;
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		standardResults.register(tree, id);
		galleryResults.register(tree, id);
		videoResults.register(tree, id);

		SearchTypeListModel typeListModel = new SearchTypeListModel();
		resultType.setListModel(typeListModel);
		resultType.setAlwaysSelect(true);
		resultType.addChangeEventHandler(events.getNamedHandler("resultTypeChanged"));
	}

	@EventHandlerMethod
	public void resultTypeChanged(SectionInfo info)
	{
		startSearch(info);
	}

	public class SearchTypeListModel extends DynamicHtmlListModel<StandardSearchResultType>
	{
		@Override
		protected KeyOption<StandardSearchResultType> convertToOption(SectionInfo info,
			StandardSearchResultType resultType)
		{
			return new KeyOption<StandardSearchResultType>(resultType.getKey(), resultType.getValue(), resultType);
		}

		@Override
		public StandardSearchResultType getValue(SectionInfo info, String value)
		{
			StandardSearchResultType val = super.getValue(info, value);
			return val == null ? getValue(info, getDefaultValue(info)) : val;
		}

		@Override
		protected Iterable<StandardSearchResultType> populateModel(SectionInfo info)
		{
			final SearchSettings settings = getSearchSettings();
			List<StandardSearchResultType> resultTypes = new ArrayList<StandardSearchResultType>();
			resultTypes.add(standardResults);
			if( !settings.isSearchingDisableGallery() )
			{
				resultTypes.add(galleryResults);
			}
			if( !settings.isSearchingDisableVideos() )
			{
				resultTypes.add(videoResults);
			}
			return resultTypes;
		}
	}

	@Override
	protected void registerItemList(SectionTree tree, String id)
	{
		// registered elsewhere
	}

	private SearchSettings getSearchSettings()
	{
		return configService.getProperties(new SearchSettings());
	}

}
