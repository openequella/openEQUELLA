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

import java.util.Set;

import com.google.inject.Inject;
import com.tle.annotation.NonNullByDefault;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemNotificationId;
import com.tle.common.search.DefaultSearch;
import com.tle.core.freetext.service.FreeTextService;
import com.tle.core.item.service.ItemService;
import com.tle.core.item.standard.ItemOperationFactory;
import com.tle.core.notification.standard.indexer.NotificationResult;
import com.tle.core.services.item.FreetextSearchResults;
import com.tle.web.bulk.section.AbstractBulkResultsDialog;
import com.tle.web.bulk.section.AbstractBulkSelectionSection;
import com.tle.web.search.base.AbstractFreetextResultsSection;
import com.tle.web.search.event.FreetextSearchEvent;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.annotation.PlugURL;
import com.tle.web.sections.equella.receipt.ReceiptService;
import com.tle.web.sections.equella.render.EquellaButtonExtension;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.validators.Confirm;
import com.tle.web.sections.render.CssInclude;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.result.util.PluralKeyLabel;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.annotations.Component;

@SuppressWarnings("nls")
@NonNullByDefault
public class NotificationSelectionSection extends AbstractBulkSelectionSection<ItemNotificationId>
{
	private static final String KEY_SELECTIONS = "notificationSelections"; //$NON-NLS-1$

	@PlugKey("notifications.selectionsbox.selectall")
	private static Label LABEL_SELECTALL;
	@PlugKey("notifications.selectionsbox.unselect")
	private static Label LABEL_UNSELECTALL;
	@PlugKey("notifications.selectionsbox.viewselected")
	private static Label LABEL_VIEWSELECTED;
	@PlugKey("notifications.selectionsbox.count")
	private static String LABEL_COUNT;
	@PlugKey("notifications.selectionsbox.clearselected")
	private static Label LABEL_CLEAR_SELECTED;
	@PlugKey("notifications.selectionsbox.pleaseselect")
	private static Label LABEL_PLEASE_SELECT;
	@PlugKey("selection.clear.receipt")
	private static String LABEL_RECEIPT;
	@PlugKey("notifications.selectionsbox.clearselected.confirm")
	private static String LABEL_CONFIRM;

	@PlugURL("css/notification.css")
	private static String CSS;

	@Component
	@Inject
	private NotificationResultsDialog bulkDialog;
	@Component
	private Button clearSelectedButton;

	@Inject
	private ItemOperationFactory workflowFactory;
	@Inject
	private ItemService itemService;
	@Inject
	private ReceiptService receiptService;
	@Inject
	private FreeTextService freeTextService;

	@EventFactory
	private EventGenerator events;

	@TreeLookup
	private AbstractFreetextResultsSection<?, ?> resultsSection;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		clearSelectedButton.setLabel(LABEL_CLEAR_SELECTED);
		clearSelectedButton.setStyleClass("clear-selected-button");
		clearSelectedButton.setDefaultRenderer(EquellaButtonExtension.ACTION_BUTTON);
		clearSelectedButton.addPrerenderables(CssInclude.include(CSS).make());
	}

	@Override
	protected void setupButton(SectionInfo context, int selectionCount)
	{
		if( selectionCount > 0 )
		{
			clearSelectedButton.setClickHandler(context, events.getNamedHandler("clearSelected")
				.addValidator(new Confirm(new PluralKeyLabel(LABEL_CONFIRM, selectionCount))));
		}
		else
		{
			clearSelectedButton.setClickHandler(context, new OverrideHandler(Js.alert_s(getPleaseSelectLabel())));
		}
	}

	@EventHandlerMethod
	public void clearSelected(SectionInfo info)
	{
		Set<ItemNotificationId> selectedNotifications = getSelections(info);
		int selectionSize = selectedNotifications.size();

		for( ItemNotificationId note : selectedNotifications )
		{
			// FIXME: this is pretty slow
			itemService.operation(new ItemId(note.getUuid(), note.getVersion()),
				workflowFactory.clearNotification(note.getNotificationId()), workflowFactory.reindexOnly(true));
		}
		unselectAll(info);
		receiptService.setReceipt(new KeyLabel(LABEL_RECEIPT, selectionSize));

	}

	@Override
	protected Label getLabelSelectAll()
	{
		return LABEL_SELECTALL;
	}

	@Override
	protected Label getLabelUnselectAll()
	{
		return LABEL_UNSELECTALL;
	}

	@Override
	protected Label getLabelViewSelected()
	{
		return LABEL_VIEWSELECTED;
	}

	@Override
	protected AbstractBulkResultsDialog<ItemNotificationId> getBulkDialog()
	{
		return bulkDialog;
	}

	@Override
	protected Label getPleaseSelectLabel()
	{
		return LABEL_PLEASE_SELECT;
	}

	@Override
	protected Label getSelectionBoxCountLabel(int selectionCount)
	{
		return new PluralKeyLabel(LABEL_COUNT, selectionCount);
	}

	@Override
	public void selectAll(SectionInfo info)
	{
		FreetextSearchEvent searchEvent = resultsSection.createSearchEvent(info);
		info.processEvent(searchEvent);
		DefaultSearch search = searchEvent.getFinalSearch();
		FreetextSearchResults<NotificationResult> results = freeTextService.search(search, 0, Integer.MAX_VALUE);
		Model<ItemNotificationId> model = getModel(info);
		Set<ItemNotificationId> selections = model.getSelections();

		int count = results.getCount();
		for( int i = 0; i < count; i++ )
		{
			NotificationResult noteResult = results.getResultData(i);
			selections.add(new ItemNotificationId(noteResult.getItemIdKey(), noteResult.getNotificationId()));
		}
		model.setModifiedSelection(true);
	}

	@Override
	protected String getKeySelections()
	{
		return KEY_SELECTIONS;
	}

	@Override
	protected boolean useBitSet()
	{
		return false;
	}

	@Override
	public Button getExecuteButton()
	{
		return clearSelectedButton;
	}
}
