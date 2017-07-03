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

package com.tle.web.itemlist.standard;

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
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.CssInclude;
import com.tle.web.sections.render.ExtraAttributes;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.TagState;
import com.tle.web.sections.result.util.PluralKeyLabel;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.renderers.DivRenderer;
import com.tle.web.sections.standard.renderers.SpanRenderer;
import com.tle.web.viewurl.ViewItemUrl;
import com.tle.web.viewurl.ViewItemUrlFactory;

@SuppressWarnings("nls")
@Bind
public class ItemListCommentsDisplaySection extends AbstractPrototypeSection<Object>
	implements
		ItemlikeListEntryExtension<Item, ItemListEntry>
{
	private static final CssInclude CSS = CssInclude
		.include(
			ResourcesService.getResourceHelper(ItemListCommentsDisplaySection.class).url("css/commentsdisplay.css"))
		.make();

	@EventFactory
	private EventGenerator events;
	@Inject
	private ItemDao itemDao;
	@Inject
	private ViewItemUrlFactory urlFactory;

	@PlugKey(value = "comments.existingcount")
	private static String X_COMMENTS_KEY;

	private static String[] RATING_CLASSES = new String[]{"zero", "one", "two", "three", "four", "five"};

	@Override
	public ProcessEntryCallback<Item, ItemListEntry> processEntries(final RenderContext context,
		List<ItemListEntry> entries, ListSettings<ItemListEntry> settings)
	{
		List<Item> items = AbstractItemlikeListEntry.getItems(entries);
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

				// srsly, this could be done with an ftl
				int size = commentCounts.get(i[0]++);
				Label label = new PluralKeyLabel(X_COMMENTS_KEY, size);
				Item item = entry.getItem();

				TagState starSpanState = new TagState();
				String alt = RATING_CLASSES[entry.getRating()] + " star rating average";
				starSpanState.addClass("screen-reader");
				starSpanState.addTagProcessor(new ExtraAttributes("tabindex", "0", "alt", alt, "title", alt));
				SpanRenderer screenReaderSpan = new SpanRenderer(starSpanState, alt);

				DivRenderer stars = new DivRenderer("itemresult-stars " + RATING_CLASSES[entry.getRating()], "");
				// FIXME:
				CSS.preRender(context.getPreRenderContext());

				entry.addRatingMetadataWithOrder(-100, screenReaderSpan, stars,
					new HtmlLinkState(label, events.getNamedHandler("seeComments", item.getItemId())));
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
