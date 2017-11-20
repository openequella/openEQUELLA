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

package com.tle.web.searching.itemlist;

import java.util.List;

import javax.inject.Inject;

import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.core.guice.Bind;
import com.tle.core.item.dao.ItemDao;
import com.tle.web.itemlist.item.AbstractItemlikeListEntry;
import com.tle.web.itemlist.item.ItemListEntry;
import com.tle.web.itemlist.item.ItemlikeListEntryExtension;
import com.tle.web.itemlist.item.ListSettings;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.result.util.IconLabel;
import com.tle.web.sections.result.util.IconLabel.Icon;
import com.tle.web.sections.result.util.NumberLabel;
import com.tle.web.sections.result.util.PluralKeyLabel;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.renderers.LinkRenderer;
import com.tle.web.viewurl.ViewItemUrl;
import com.tle.web.viewurl.ViewItemUrlFactory;

@Bind
public class CommentCountGalleryDisplay extends AbstractPrototypeSection<Object>
	implements
		ItemlikeListEntryExtension<Item, ItemListEntry>
{
	@EventFactory
	private EventGenerator events;
	@Inject
	private ItemDao itemDao;
	@Inject
	private ViewItemUrlFactory urlFactory;

	@PlugKey(value = "comments.existingcount")
	private static String X_COMMENTS_KEY;

	@Override
	public ProcessEntryCallback<Item, ItemListEntry> processEntries(RenderContext context, List<ItemListEntry> entries,
		ListSettings<ItemListEntry> listSettings)
	{
		List<Item> items = AbstractItemlikeListEntry.getItems(entries);
		// GROSS
		final List<Integer> commentCounts = itemDao.getCommentCounts(items);
		final int i[] = new int[]{0};
		return new ProcessEntryCallback<Item, ItemListEntry>()
		{
			@Override
			public void processEntry(ItemListEntry entry)
			{
				if( entry.isFlagSet("com.tle.web.viewitem.DontShowRating") )
				{
					return;
				}
				int size = commentCounts.get(i[0]++);
				Label label = new PluralKeyLabel(X_COMMENTS_KEY, size);
				HtmlLinkState link = new HtmlLinkState(new IconLabel(Icon.COMMENT, new NumberLabel(size), true));
				link.setTitle(label);
				link.setClickHandler(events.getNamedHandler("seeComments", entry.getItem().getItemId()));
				link.addClass("gallery-action");
				entry.addRatingMetadataWithOrder(300, new LinkRenderer(link));
			}
		};
	}

	@EventHandlerMethod
	public void seeComments(SectionInfo info, String itemId)
	{
		ViewItemUrl commentsUrl = urlFactory.createItemUrl(info, new ItemId(itemId));
		commentsUrl.setAnchor("#comments");
		commentsUrl.forward(info);
	}

	@Override
	public void register(SectionTree tree, String parentId)
	{
		tree.registerInnerSection(this, parentId);
	}

	@Override
	public Class<Object> getModelClass()
	{
		return Object.class;
	}

	@Override
	public String getItemExtensionType()
	{
		return null;
	}

}
