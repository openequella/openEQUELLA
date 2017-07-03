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

import java.util.List;

import javax.inject.Inject;

import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.core.favourites.service.BookmarkService;
import com.tle.core.guice.Bind;
import com.tle.core.item.service.ItemService;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.web.favourites.FavouritesDialog;
import com.tle.web.itemlist.item.AbstractItemlikeListEntry;
import com.tle.web.itemlist.item.ItemListEntry;
import com.tle.web.itemlist.item.ItemlikeListEntryExtension;
import com.tle.web.itemlist.item.ListSettings;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.receipt.ReceiptService;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.validators.Confirm;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.result.util.IconLabel;
import com.tle.web.sections.result.util.IconLabel.Icon;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.HtmlLinkState;

@SuppressWarnings("nls")
@Bind
public class FavouritesDisplay extends AbstractPrototypeSection<Object>
	implements
		ItemlikeListEntryExtension<Item, ItemListEntry>
{
	@PlugKey("searchresults.add")
	private static Label ADD_LABEL;
	@PlugKey("searchresults.add.receipt")
	private static Label ADD_RECEIPT_LABEL;
	@PlugKey("searchresults.remove")
	private static Label REMOVE_LABEL;
	@PlugKey("searchresults.remove.confirm")
	private static Label REMOVE_CONFIRM_LABEL;
	@PlugKey("searchresults.remove.receipt")
	private static Label REMOVE_RECEIPT_LABEL;

	@EventFactory
	private EventGenerator events;

	@Inject
	private BookmarkService bookmarkService;
	@Inject
	private ItemService itemService;
	@Inject
	private ReceiptService receiptService;

	@Inject
	@Component
	private FavouritesDialog dialog;

	private JSCallable removeFunc;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		removeFunc = events.getSubmitValuesFunction("removeFavourite");

		dialog.setOkCallback(events.getSubmitValuesFunction("addFavourite"));
	}

	@Override
	public ProcessEntryCallback<Item, ItemListEntry> processEntries(RenderContext context, List<ItemListEntry> entries,
		final ListSettings<ItemListEntry> settings)
	{

		if( CurrentUser.wasAutoLoggedIn() )
		{
			return null;
		}

		final List<Item> bookmarked = bookmarkService.filterNonBookmarkedItems(AbstractItemlikeListEntry
			.getItems(entries));

		return new ProcessEntryCallback<Item, ItemListEntry>()
		{
			@Override
			public void processEntry(ItemListEntry entry)
			{
				if( entry.isFlagSet("com.tle.web.favourites.DontShow") )
				{
					return;
				}

				final Item item = entry.getItem();
				final HtmlLinkState link;
				final boolean isBookmarked = bookmarked.contains(item);

				if( !CurrentUser.isGuest() )
				{
					// string literal so don't have to include
					// com.tle.web.searching
					if( settings.getAttribute("gallery.result") != null
						|| settings.getAttribute("video.result") != null )
					{
						link = new HtmlLinkState(isBookmarked ? new IconLabel(Icon.FAVOURITES, null, true)
							: new IconLabel(Icon.FAVOURITES_EMPTY, null, true));
						link.setTitle(isBookmarked ? REMOVE_LABEL : ADD_LABEL);
						link.addClass("gallery-action");
					}
					else
					{
						link = new HtmlLinkState(isBookmarked ? REMOVE_LABEL : ADD_LABEL);
					}

					if( isBookmarked )
					{
						link.setClickHandler(new OverrideHandler(removeFunc, item.getItemId())
							.addValidator(new Confirm(REMOVE_CONFIRM_LABEL)));
					}
					else
					{
						link.setClickHandler(new OverrideHandler(dialog.getOpenFunction(), item.getItemId()));
					}
					entry.addRatingMetadata(link);
				}
			}
		};
	}

	@EventHandlerMethod
	public void removeFavourite(SectionInfo info, String itemId)
	{
		bookmarkService.delete(bookmarkService.getByItem(new ItemId(itemId)).getId());
		receiptService.setReceipt(REMOVE_RECEIPT_LABEL);
	}

	@EventHandlerMethod
	public void addFavourite(SectionInfo info, String tagString, boolean latest, String itemId)
	{
		Item item = itemService.get(new ItemId(itemId));
		bookmarkService.add(item, tagString, latest);
		receiptService.setReceipt(ADD_RECEIPT_LABEL);
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
