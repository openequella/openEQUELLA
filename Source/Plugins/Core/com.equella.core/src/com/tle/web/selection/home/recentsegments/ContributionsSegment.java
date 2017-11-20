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

package com.tle.web.selection.home.recentsegments;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import com.tle.beans.item.Item;
import com.tle.beans.item.ItemStatus;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.search.DefaultSearch;
import com.tle.common.searching.Search.SortType;
import com.tle.common.searching.SearchResults;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.core.freetext.service.FreeTextService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.selection.SelectionService;
import com.tle.web.selection.SelectionSession;
import com.tle.web.selection.home.model.RecentSelectionSegmentModel.RecentSelection;
import com.tle.web.viewurl.ViewItemUrlFactory;

/**
 * @author aholland
 */
public class ContributionsSegment extends AbstractRecentSegment
{
	@PlugKey("recently.contributed")
	private static Label TITLE;
	@Inject
	private FreeTextService searchService;
	@Inject
	private ViewItemUrlFactory urlFactory;
	@Inject
	private SelectionService selectionService;

	@Override
	protected List<RecentSelection> getSelections(SectionInfo info, SelectionSession session, int maximum)
	{
		List<RecentSelection> selections = new ArrayList<RecentSelection>();
		DefaultSearch search = new DefaultSearch();
		search.setOwner(CurrentUser.getUserID());
		search.setPrivilege(selectionService.getSearchPrivilege(info));
		if( !session.isAllCollections() )
		{
			Set<String> collectionUuids = session.getCollectionUuids();
			if( !collectionUuids.isEmpty() )
			{
				search.setCollectionUuids(collectionUuids);
			}
			else
			{
				// there is nothing available...
				return selections;
			}
		}
		Set<String> mimeTypes = session.getMimeTypes();
		if( mimeTypes != null && !mimeTypes.isEmpty() )
		{
			search.setMimeTypes(mimeTypes);
		}

		search.setNotItemStatuses(ItemStatus.PERSONAL);
		search.setSortType(SortType.DATEMODIFIED);

		SearchResults<Item> results = searchService.search(search, 0, maximum);
		List<Item> items = results.getResults();
		for( Item item : items )
		{
			HtmlLinkState state = new HtmlLinkState(urlFactory.createItemUrl(info, item.getItemId()));
			selections.add(new RecentSelection(CurrentLocale.get(item.getName(), item.getIdString()), state));
		}
		return selections;
	}

	@Override
	public String getTitle(SectionInfo info, SelectionSession session)
	{
		return TITLE.getText(); //$NON-NLS-1$
	}
}
