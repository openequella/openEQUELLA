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

package com.tle.mycontent.web.search;

import java.util.List;

import javax.inject.Inject;

import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemStatus;
import com.tle.core.guice.Bind;
import com.tle.mycontent.service.MyContentService;
import com.tle.web.itemlist.item.ItemListEntry;
import com.tle.web.itemlist.item.ItemlikeListEntryExtension;
import com.tle.web.itemlist.item.ListSettings;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.receipt.ReceiptService;
import com.tle.web.sections.equella.render.ButtonRenderer;
import com.tle.web.sections.equella.render.ButtonRenderer.ButtonType;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.js.validators.Confirm;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.model.HtmlLinkState;

@Bind
public class MyContentItemListExtension extends AbstractPrototypeSection<Object>
	implements
		ItemlikeListEntryExtension<Item, ItemListEntry>
{
	@PlugKey("edit")
	private static Label LABEL_EDIT;
	@PlugKey("delete")
	private static Label LABEL_DELETE;
	@PlugKey("restore")
	private static Label LABEL_RESTORE;
	@PlugKey("deleteconfirm")
	private static Label LABEL_DELETECONFIRM;
	@PlugKey("deletereceipt")
	private static Label LABEL_DELETE_RECEIPT;
	@PlugKey("restorereceipt")
	private static Label LABEL_RESTORE_RECEIPT;

	@EventFactory
	private EventGenerator events;

	@Inject
	private MyContentService myContentService;
	@Inject
	private ReceiptService receiptService;

	@EventHandlerMethod
	public void edit(SectionInfo info, ItemId itemId)
	{
		myContentService.forwardToEditor(info, itemId);
	}

	@EventHandlerMethod
	public void delete(SectionInfo info, ItemId itemId)
	{
		myContentService.delete(itemId);
		receiptService.setReceipt(LABEL_DELETE_RECEIPT);
	}

	@EventHandlerMethod
	public void restore(SectionInfo info, ItemId itemId)
	{
		myContentService.restore(itemId);
		receiptService.setReceipt(LABEL_RESTORE_RECEIPT);
	}

	@SuppressWarnings("nls")
	@Override
	public ProcessEntryCallback<Item, ItemListEntry> processEntries(RenderContext context, List<ItemListEntry> entries,
		final ListSettings<ItemListEntry> settings)
	{

		return new ProcessEntryCallback<Item, ItemListEntry>()
		{
			@Override
			public void processEntry(ItemListEntry entry)
			{
				Item item = entry.getItem();
				if( entry instanceof MyContentItemListEntry )
				{
					if( settings.isEditable() )
					{
						if( item.getStatus() == ItemStatus.DELETED )
						{
							HtmlLinkState state = new HtmlLinkState(LABEL_RESTORE, events.getNamedHandler("restore",
								item.getItemId()));
							entry.addRatingAction(new ButtonRenderer(state).showAs(ButtonType.ADD));
						}
						else
						{
							HtmlLinkState edit = new HtmlLinkState(LABEL_EDIT, events.getNamedHandler("edit",
								item.getItemId()));
							entry.addRatingAction(new ButtonRenderer(edit).showAs(ButtonType.EDIT));

							HtmlLinkState delete = new HtmlLinkState(LABEL_DELETE, events.getNamedHandler("delete",
								item.getItemId()).addValidator(new Confirm(LABEL_DELETECONFIRM)));
							entry.addRatingAction(new ButtonRenderer(delete).showAs(ButtonType.DELETE));
						}
					}
				}
			}
		};
	}

	@Override
	public void register(SectionTree tree, String parentId)
	{
		tree.registerInnerSection(this, parentId);
	}

	@Override
	public String getItemExtensionType()
	{
		return null;
	}
}
