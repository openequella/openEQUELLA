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

package com.tle.web.notification.section;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import com.google.inject.Provider;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemNotificationId;
import com.tle.core.guice.Bind;
import com.tle.core.item.service.ItemService;
import com.tle.core.item.standard.ItemOperationFactory;
import com.tle.core.notification.NotificationService;
import com.tle.core.notification.beans.Notification;
import com.tle.core.notification.standard.indexer.NotificationResult;
import com.tle.core.services.item.FreetextResult;
import com.tle.web.itemlist.item.AbstractItemList;
import com.tle.web.notification.NotificationItemListEntry;
import com.tle.web.notification.WebNotificationExtension;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ajax.AjaxRenderContext;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.receipt.ReceiptService;
import com.tle.web.sections.equella.render.ButtonRenderer;
import com.tle.web.sections.equella.render.ButtonRenderer.ButtonType;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.generic.SimpleElementId;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.model.HtmlLinkState;

@Bind
public class NotificationItemList
	extends
		AbstractItemList<NotificationItemListEntry, AbstractItemList.Model<NotificationItemListEntry>>
{
	@SuppressWarnings("nls")
	private static final String DIV_PFX = "not_";

	@PlugKey("notificationlist.clear")
	private static Label LABEL_CLEAR;
	@PlugKey("notificationlist.clear.receipt")
	private static Label LABEL_CLEAR_RECEIPT;
	@PlugKey("notificationlist.reason")
	private static Label LABEL_REASON;
	@PlugKey("selectitem")
	private static Label LABEL_SELECT;
	@PlugKey("unselectitem")
	private static Label LABEL_UNSELECT;

	@EventFactory
	private EventGenerator events;
	@TreeLookup
	private NotificationSelectionSection selectionSection;

	@Inject
	private NotificationService notificationService;
	@Inject
	private ItemService itemService;
	@Inject
	private ReceiptService receiptService;
	@Inject
	private ItemOperationFactory workflowFactory;
	@Inject
	private Provider<NotificationItemListEntry> entryFactory;

	private JSCallable selectCall;
	private JSCallable removeCall;

	@SuppressWarnings("nls")
	@Override
	protected void customiseListEntries(RenderContext context, List<NotificationItemListEntry> entries)
	{
		for( NotificationItemListEntry entry : entries )
		{
			Item item = entry.getItem();
			ItemNotificationId notificantionId = new ItemNotificationId(item.getItemId(), entry.getNotificationId());
			entry.getTag().setElementId(new SimpleElementId(DIV_PFX + notificantionId.toString()));
			Notification notification = notificationService.getNotification(notificantionId.getNotificationId());
			if( notification != null )
			{
				String reason = notification.getReason();
				WebNotificationExtension extension = (WebNotificationExtension) notificationService
					.getExtensionForType(reason);
				entry.addDelimitedMetadata(LABEL_REASON, extension.getReasonLabel(reason));

				HtmlLinkState state = new HtmlLinkState(LABEL_CLEAR,
					events.getNamedHandler("clear", entry.getItem().getItemId(), notificantionId.getNotificationId()));
				entry.addRatingAction(new ButtonRenderer(state).showAs(ButtonType.DELETE));
			}

			if( !selectionSection.isSelected(context, notificantionId) )
			{
				HtmlLinkState link = new HtmlLinkState(LABEL_SELECT,
					new OverrideHandler(selectCall, item.getUuid(), item.getVersion(), entry.getNotificationId()));
				entry.addRatingAction(new ButtonRenderer(link).showAs(ButtonType.SELECT));
			}
			else
			{
				HtmlLinkState link = new HtmlLinkState(LABEL_UNSELECT,
					new OverrideHandler(removeCall, item.getUuid(), item.getVersion(), entry.getNotificationId()));
				entry.addRatingAction(new ButtonRenderer(link).showAs(ButtonType.UNSELECT));
				entry.setSelected(true);
			}

		}
		super.customiseListEntries(context, entries);
	}

	@EventHandlerMethod
	public void selectItem(SectionInfo info, String uuid, int ver, long notificationId)
	{
		ItemNotificationId noteId = new ItemNotificationId(uuid, ver, notificationId);
		selectionSection.addSelection(info, noteId);
		addAjaxDiv(info, noteId);
	}

	private void addAjaxDiv(SectionInfo info, ItemNotificationId noteId)
	{
		AjaxRenderContext renderContext = info.getAttributeForClass(AjaxRenderContext.class);
		if( renderContext != null )
		{
			renderContext.addAjaxDivs(DIV_PFX + noteId.toString());
		}
	}

	@EventHandlerMethod
	public void removeItem(SectionInfo info, String uuid, int ver, long notificationId)
	{
		ItemNotificationId noteId = new ItemNotificationId(uuid, ver, notificationId);
		selectionSection.removeSelection(info, noteId);
		addAjaxDiv(info, noteId);
	}

	@EventHandlerMethod
	public void clear(SectionInfo info, ItemId itemId, long notificationId)
	{
		itemService.operation(itemId, workflowFactory.clearNotification(notificationId),
			workflowFactory.reindexOnly(true));
		receiptService.setReceipt(LABEL_CLEAR_RECEIPT);
	}

	@SuppressWarnings("nls")
	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		super.treeFinished(id, tree);
		selectCall = selectionSection.getUpdateSelection(tree, events.getEventHandler("selectItem"));
		removeCall = selectionSection.getUpdateSelection(tree, events.getEventHandler("removeItem"));
	}

	@SuppressWarnings("nls")
	@Override
	protected Set<String> getExtensionTypes()
	{
		return Collections.singleton("notification");
	}

	@Override
	protected NotificationItemListEntry createItemListEntry(SectionInfo info, Item item, FreetextResult result)
	{
		NotificationItemListEntry notificationItemListItem = entryFactory.get();
		notificationItemListItem.setInfo(info);
		notificationItemListItem.setItem(item);
		return notificationItemListItem;
	}

	@SuppressWarnings("nls")
	@Override
	public NotificationItemListEntry addItem(SectionInfo info, Item item, FreetextResult resultData)
	{
		if( resultData instanceof NotificationResult )
		{
			NotificationResult result = (NotificationResult) resultData;
			NotificationItemListEntry entry = createItemListEntry(info, item, resultData);
			entry.setNotificationId(result.getNotificationId());
			addListItem(info, entry);
			return entry;
		}
		else
		{
			throw new RuntimeException("How did this even get in here");
		}
	}
}
