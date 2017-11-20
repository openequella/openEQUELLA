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

package com.tle.web.searching.prevnext;

import javax.inject.Inject;

import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemKey;
import com.tle.common.search.DefaultSearch;
import com.tle.core.freetext.service.FreeTextService;
import com.tle.core.guice.Bind;
import com.tle.core.search.MappedSearchIndexValues;
import com.tle.core.services.item.FreetextResult;
import com.tle.core.services.item.FreetextSearchResults;
import com.tle.core.services.user.UserSessionService;
import com.tle.web.search.event.FreetextSearchEvent;
import com.tle.web.searching.section.SearchResultsSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.viewable.NewDefaultViewableItem;
import com.tle.web.viewable.impl.ViewableItemFactory;
import com.tle.web.viewitem.section.AbstractParentViewItemSection;
import com.tle.web.viewurl.ItemSectionInfo;
import com.tle.web.viewurl.ViewItemUrl;
import com.tle.web.viewurl.ViewItemUrlFactory;

/**
 * @author larry
 */
@Bind
@SuppressWarnings("nls")
public class SearchPrevNextSection extends AbstractParentViewItemSection<SearchPrevNextSection.SearchPrevNextModel>
{
	private static final int ONE_STEP_BACKWARDS = -1;
	private static final int ONE_STEP_FORWARDS = 1;

	@Component(name = "btnprev")
	@PlugKey("button.prev")
	private Button prevButton;

	@Component(name = "btnnext")
	@PlugKey("button.next")
	private Button nextButton;

	@EventFactory
	private EventGenerator events;

	@Inject
	private UserSessionService sessionService;
	@Inject
	private ViewItemUrlFactory urlFactory;
	@Inject
	private FreeTextService freetextService;
	@Inject
	private ViewableItemFactory viewableItemFactory;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		prevButton.setClickHandler(events.getNamedHandler("nav", ONE_STEP_BACKWARDS));
		nextButton.setClickHandler(events.getNamedHandler("nav", ONE_STEP_FORWARDS));
	}

	@EventHandlerMethod
	public void nav(SectionInfo info, int direction)
	{
		int currentIndexIntoSearch = verifyStateSetup(info);
		int newIndex = currentIndexIntoSearch + direction;
		ItemKey itemKeyToView = null;
		SearchPrevNextModel model = getModel(info);
		int offsetForKeyResourceSize = 0;

		ItemId targettedItemId = null;
		Object mapAttrObj = sessionService.getAttribute(MappedSearchIndexValues.MAPPED_SEARCH_ATTR_KEY);
		if( mapAttrObj != null )
		{
			MappedSearchIndexValues indexMap = (MappedSearchIndexValues) mapAttrObj;
			offsetForKeyResourceSize = indexMap.getOffsetHandicap();
			for( ItemId key : indexMap.getIndexMap().keySet() )
			{
				if( indexMap.getIndexForItemId(key) == newIndex )
				{
					targettedItemId = key;
					break;
				}
			}
		}
		if( targettedItemId != null )
		{
			itemKeyToView = targettedItemId;
		}
		else
		{
			// need to reach/create a FreetextSearchEvent - first look in the
			// session if the session has a MappedSearchIndexValues, use its
			// activeSearch if it exists
			MappedSearchIndexValues indexMap = mapAttrObj != null ? (MappedSearchIndexValues) mapAttrObj : null;
			DefaultSearch search = indexMap != null ? indexMap.getActiveSearch() : null;

			if( search == null )
			{
				// otherwise we can assume the event was a standard search event
				SectionInfo forward = info.createForward("/searching.do");
				SearchResultsSection resultsSection = forward.lookupSection(SearchResultsSection.class);
				FreetextSearchEvent event = resultsSection.createSearchEvent(forward);
				forward.processEvent(event);
				search = event.getFinalSearch();
			}

			if( newIndex < 0 )
			{
				// We shouldn't ever be asking for the item before the first,
				// but should it be so, just return the first
				newIndex = 0;
			}
			// For a straightforward search (including simple browses), the
			// effectiveRelativeOffset is 0, but in cases where a browse search
			// is a compound between configured key resources and a freetext
			// search, we need to be able to set the offset marker back into the
			// freetext search results
			int effectiveRelativeOffset = newIndex;
			if( offsetForKeyResourceSize > 0 )
			{
				if( newIndex < offsetForKeyResourceSize )
				{
					// in this case we're into the key or dynamic resources of
					// the browse search, not the general freetext part
					if( indexMap != null ) // we hardly expect otherwise
					{
						itemKeyToView = indexMap.getKeyResourceItemIds().get(newIndex);
					}
				}
				else
				{
					effectiveRelativeOffset = newIndex - offsetForKeyResourceSize;
				}
			}

			if( itemKeyToView == null )
			{
				FreetextSearchResults<FreetextResult> singleSearchResultForAdjacent = freetextService.search(search,
					effectiveRelativeOffset, 1);

				int totalNumResults = singleSearchResultForAdjacent.getAvailable();
				if( totalNumResults == 0 )
				{
					SectionInfo forward = info.createForward("/searching.do");
					info.forwardAsBookmark(forward);
					return;
				}
				if( singleSearchResultForAdjacent.getCount() == 0 )
				{
					// This shouldn't happen unless were at the penultimate, and
					// the 'next' expected item was suddenly made unavailable.
					// Just set the index-to-retrieve as the last in the search
					// list.
					newIndex = totalNumResults - 1;
					singleSearchResultForAdjacent = freetextService.search(search, newIndex, 1);
					totalNumResults = singleSearchResultForAdjacent.getAvailable();
					if( totalNumResults == 0 )
					{
						SectionInfo forward = info.createForward("/searching.do");
						info.forwardAsBookmark(forward);
						return;
					}
				}

				FreetextResult singleResult = singleSearchResultForAdjacent.getResultData(0);
				itemKeyToView = singleResult.getItemIdKey();
			}
		}

		// if we've passed beyond the limited paged result set so far mapped, we
		// may need to manually insert the new ItemId into the session map
		ItemId keyAsItemId = new ItemId(itemKeyToView.getUuid(), itemKeyToView.getVersion());
		model.setCurrentItemKey(keyAsItemId);
		if( model.getIndexIntoSearch() < 0 )
		{
			model.getMappedSearchIndexValues().mapItemIdWithIndex(keyAsItemId, newIndex, false);
		}
		// show this as the single result ...
		NewDefaultViewableItem viewable = viewableItemFactory.createNewViewableItem(itemKeyToView);
		Item item = viewable.getItem();
		ViewItemUrl vurl = urlFactory.createItemUrl(info, item.getItemId());
		vurl.getQueryString(); // .. side effects?
		SectionInfo sinfo = vurl.getSectionInfo();
		info.forwardAsBookmark(sinfo);
	}

	/**
	 * Visibility is set in the admin console for collections. Specific
	 * visibility here depends on whether the search result set had more than
	 * one result.
	 * 
	 * @return true if section is viewable, otherwise false
	 */
	@Override
	public boolean canView(SectionInfo info)
	{
		final ItemSectionInfo itemInfo = getItemInfo(info);
		int index = verifyStateSetup(info);
		if( index >= 0 && itemInfo.getViewableItem().isItemForReal() )
		{
			return true;
		}
		// else nothing to show
		return false;
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		if( canView(context) )
		{
			SearchPrevNextModel model = getModel(context);

			// If the current item is the first, disable previous button
			if( model.getIndexIntoSearch() == 0 )
			{
				getPrevButton().disable(context);
			}
			// If the current item is last available, disable next button
			if( model.getIndexIntoSearch() == model.getAvailableForSearch() - 1 )
			{
				getNextButton().disable(context);
			}
			return viewFactory.createNamedResult("section_comments", "search_prev_next.ftl", this);
		}
		return null;
	}

	public Button getPrevButton()
	{
		return prevButton;
	}

	public Button getNextButton()
	{
		return nextButton;
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "prevnextbuttons";
	}

	/**
	 * When called from nav (click of either forward/back button) we don't want
	 * to reset the model state.
	 * 
	 * @param info
	 * @return
	 */
	private int verifyStateSetup(SectionInfo info)
	{
		SearchPrevNextModel model = getModel(info);
		if( !model.isStateSet() )
		{
			Object mapAttrObj = sessionService.getAttribute(MappedSearchIndexValues.MAPPED_SEARCH_ATTR_KEY);
			if( mapAttrObj != null )
			{
				ItemSectionInfo itemSectionInfo = getItemInfo(info);
				MappedSearchIndexValues indexMap = (MappedSearchIndexValues) mapAttrObj;
				model.setMapAndKey(indexMap, itemSectionInfo.getItem().getItemId());
			}
			model.setStateSet(true);
		}
		return model.getIndexIntoSearch();
	}

	@Override
	public Class<SearchPrevNextModel> getModelClass()
	{
		return SearchPrevNextModel.class;
	}

	public static class SearchPrevNextModel
	{
		/**
		 * this is a reference to an entity held in the session, hence changes
		 * to it will persist beyond the lifetime of the model instance
		 */
		private MappedSearchIndexValues mappedSearchIndexValues;
		private ItemId currentItemKey;
		private boolean stateSet;

		public boolean isStateSet()
		{
			return stateSet;
		}

		public void setStateSet(boolean stateSet)
		{
			this.stateSet = stateSet;
		}

		public int getIndexIntoSearch()
		{
			return mappedSearchIndexValues != null ? mappedSearchIndexValues.getIndexForItemId(currentItemKey) : -1;
		}

		public int getAvailableForSearch()
		{
			return mappedSearchIndexValues != null ? mappedSearchIndexValues.getAvailable() : -1;
		}

		public MappedSearchIndexValues getMappedSearchIndexValues()
		{
			return mappedSearchIndexValues;
		}

		public void setMapAndKey(MappedSearchIndexValues mappedSearchIndexValues, ItemId key)
		{
			this.mappedSearchIndexValues = mappedSearchIndexValues;
			this.currentItemKey = key;
		}

		public void setCurrentItemKey(ItemId key)
		{
			this.currentItemKey = key;
		}
	}
}
