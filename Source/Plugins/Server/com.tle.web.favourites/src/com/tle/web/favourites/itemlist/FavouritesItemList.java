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

package com.tle.web.favourites.itemlist;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.tle.beans.item.Bookmark;
import com.tle.beans.item.Item;
import com.tle.core.favourites.service.BookmarkService;
import com.tle.core.guice.Bind;
import com.tle.web.itemlist.StdMetadataEntry;
import com.tle.web.itemlist.item.AbstractItemlikeListEntry;
import com.tle.web.itemlist.item.StandardItemList;
import com.tle.web.itemlist.item.StandardItemListEntry;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.render.DateRendererFactory;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.render.Label;

@Bind
public class FavouritesItemList extends StandardItemList
{
	@PlugKey("keywords")
	private static Label KEYWORDS_LABEL;
	@PlugKey("datefavourited")
	private static Label DATE_FAVOURITED_LABEL;

	@Inject
	private BookmarkService bookmarkService;
	@Inject
	private DateRendererFactory dateRendererFactory;

	@Override
	protected void customiseListEntries(RenderContext context, List<StandardItemListEntry> entries)
	{
		super.customiseListEntries(context, entries);
		final Map<Item, Bookmark> bookmarks = bookmarkService.getBookmarksForItems(AbstractItemlikeListEntry
			.getItems(entries));

		for( StandardItemListEntry entry : entries )
		{
			Bookmark b = bookmarks.get(entry.getItem());
			if( b != null )
			{
				Collection<String> keywords = b.getKeywords();
				if( !keywords.isEmpty() )
				{
					entry.addDelimitedMetadata(KEYWORDS_LABEL, keywords);
				}

				entry.addMetadata(new StdMetadataEntry(DATE_FAVOURITED_LABEL, dateRendererFactory.createDateRenderer(b
					.getDateModified())));
			}
		}
	}
}
