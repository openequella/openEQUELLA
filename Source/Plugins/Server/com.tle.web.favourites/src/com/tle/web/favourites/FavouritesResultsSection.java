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

package com.tle.web.favourites;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import com.dytech.edge.queries.FreeTextQuery;
import com.tle.common.search.DefaultSearch;
import com.tle.common.searching.Field;
import com.tle.common.settings.standard.SearchSettings;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.core.settings.service.ConfigurationService;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.itemlist.item.AbstractItemList;
import com.tle.web.itemlist.item.StandardItemListEntry;
import com.tle.web.search.base.AbstractFreetextResultsSection;
import com.tle.web.search.base.AbstractSearchResultsSection.SearchResultsModel;
import com.tle.web.search.base.ContextableSearchSection;
import com.tle.web.search.event.FreetextSearchEvent;
import com.tle.web.search.event.FreetextSearchResultEvent;
import com.tle.web.searching.GallerySearchResults;
import com.tle.web.searching.StandardSearchResultType;
import com.tle.web.searching.VideoSearchResults;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.search.AbstractSearchActionsSection;
import com.tle.web.sections.equella.utils.KeyOption;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.DynamicHtmlListModel;
import com.tle.web.template.section.event.BlueBarEvent;
import com.tle.web.template.section.event.BlueBarEventListener;

@SuppressWarnings("nls")
public class FavouritesResultsSection extends AbstractFreetextResultsSection<StandardItemListEntry, SearchResultsModel>
	implements
		BlueBarEventListener
{
	@PlugKey("noresults.items")
	private static Label LABEL_NOAVAILABLE;
	@PlugKey("noresults.items.filtered")
	private static Label LABEL_NORESULTS;

	@ViewFactory
	private FreemarkerFactory viewFactory;

	@TreeLookup
	private AbstractSearchActionsSection<?> searchActionsSection;

	@EventFactory
	private EventGenerator events;

	@Inject
	private GallerySearchResults galleryResults;
	@Inject
	private VideoSearchResults videoResults;
	@Inject
	private FavouritesSearchResults favouriteStandardResults;
	@Inject
	private ConfigurationService configService;

	@Component(parameter = "type", supported = true, contexts = ContextableSearchSection.HISTORYURL_CONTEXT)
	private SingleSelectionList<StandardSearchResultType> resultType;

	@Override
	protected SingleSelectionList<StandardSearchResultType> getResultTypeSelector(SectionInfo info)
	{
		return resultType;
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		favouriteStandardResults.register(tree, id);
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

	@Override
	public AbstractItemList<StandardItemListEntry, ?> getItemList(SectionInfo info)
	{
		return resultType.getSelectedValue(info).getCustomItemList();
	}

	@Override
	protected DefaultSearch createDefaultSearch(SectionInfo info)
	{
		FavouritesSearch search = new FavouritesSearch();
		if( resultType.getSelectedValue(info).isDisabled() )
		{
			resultType.setSelectedValue(info, favouriteStandardResults);
		}
		StandardSearchResultType selectedResults = resultType.getSelectedValue(info);
		selectedResults.addResultTypeDefaultRestrictions(search);
		return search;
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
			resultTypes.add(favouriteStandardResults);
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
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		String selectedValue = resultType.getSelectedValueAsString(context);
		Set<String> matchingValues = resultType.getListModel().getMatchingValues(context,
			Collections.singleton(selectedValue));
		if( matchingValues.size() == 0 )
		{
			String defaultValue = resultType.getListModel().getDefaultValue(context);
			resultType.setSelectedStringValue(context, defaultValue);
		}
		getPaging().setIsGalleryOptions(context, resultType.getSelectedValue(context).equals(galleryResults));
		getPaging().setIsVideoGallery(context, resultType.getSelectedValue(context).equals(videoResults));
		searchActionsSection.disableSaveAndShare(context);
		return super.renderHtml(context);
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

	public static class FavouritesSearch extends DefaultSearch
	{
		private static final long serialVersionUID = 1L;

		@Override
		public void addExtraMusts(List<List<Field>> musts)
		{
			musts
				.add(Collections.singletonList(new Field(FreeTextQuery.FIELD_BOOKMARK_OWNER, CurrentUser.getUserID())));
		}

		@Override
		public List<String> getExtraQueries()
		{
			return Collections.singletonList(String.format("%s:(%s)", FreeTextQuery.FIELD_BOOKMARK_TAGS, getQuery()));
		}
	}

	@Override
	public void addBlueBarResults(RenderContext context, BlueBarEvent event)
	{
		event.addHelp(viewFactory.createResult("helpfavouritesresources.ftl", this));
	}

	@Override
	protected void registerItemList(SectionTree tree, String id)
	{
		// done elsewhere
	}

	private SearchSettings getSearchSettings()
	{
		return configService.getProperties(new SearchSettings());
	}
}
