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

import java.util.ArrayList;
import java.util.List;

import com.google.inject.Inject;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemNotificationId;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.guice.Bind;
import com.tle.core.item.service.ItemService;
import com.tle.core.notification.NotificationService;
import com.tle.core.notification.beans.Notification;
import com.tle.web.bulk.operation.BulkOperationExtension.OperationInfo;
import com.tle.web.bulk.section.AbstractBulkResultsDialog;
import com.tle.web.bulk.section.AbstractBulkSelectionSection;
import com.tle.web.notification.WebNotificationExtension;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.result.util.PluralKeyLabel;
import com.tle.web.sections.standard.RendererConstants;
import com.tle.web.sections.standard.model.DynamicHtmlListModel;
import com.tle.web.sections.standard.model.HtmlComponentState;

@NonNullByDefault
@Bind
public class NotificationResultsDialog extends AbstractBulkResultsDialog<ItemNotificationId>
{
	@PlugKey("opresults.count")
	private static String OPRESULTS_COUNT_KEY;

	@Inject
	private NotificationService notificationService;
	@Inject
	private ItemService itemService;

	@TreeLookup
	private AbstractBulkSelectionSection<ItemNotificationId> selectionSection;

	@Override
	protected DynamicHtmlListModel<OperationInfo> getBulkOperationList(SectionTree tree, String parentId)
	{
		// there are no bulk operations for notifications
		return new DynamicHtmlListModel<OperationInfo>()
		{
			@Nullable
			@Override
			protected Iterable<OperationInfo> populateModel(SectionInfo info)
			{
				return null;
			}
		};
	}

	@Override
	protected Label getOpResultCountLabel(int totalSelections)
	{
		return new PluralKeyLabel(OPRESULTS_COUNT_KEY, totalSelections);
	}

	@Override
	protected List<SelectionRow> getRows(List<ItemNotificationId> pageOfIds)
	{
		List<SelectionRow> rows = new ArrayList<SelectionRow>();
		for( ItemNotificationId noteId : pageOfIds )
		{
			Notification itemNotification = notificationService.getNotification(noteId.getNotificationId());
			Item item = itemService.get(noteId);

			String itemName = CurrentLocale.get(item.getName(), item.getUuid());
			String reason = itemNotification.getReason();
			WebNotificationExtension extension = (WebNotificationExtension) notificationService
				.getExtensionForType(reason);
			Label reasonLabel = extension.getReasonFilterLabel(reason);
			Label description = new TextLabel(itemName + " - " + reasonLabel.getText()); //$NON-NLS-1$
			// TODO: reason is different from dialog to item list
			rows.add(new SelectionRow(description, new HtmlComponentState(RendererConstants.LINK, events
				.getNamedHandler("removeSelection", noteId.getUuid(), //$NON-NLS-1$
					noteId.getVersion(), noteId.getNotificationId()))));
		}

		return rows;
	}

	@EventHandlerMethod
	public void removeSelection(SectionInfo info, String uuid, int version, long activationId)
	{
		selectionSection.removeSelection(info, new ItemNotificationId(uuid, version, activationId));
	}

}
