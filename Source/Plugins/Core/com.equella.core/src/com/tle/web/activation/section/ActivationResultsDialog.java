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

package com.tle.web.activation.section;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.activation.ActivateRequest;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemActivationId;
import com.tle.beans.item.attachments.UnmodifiableAttachments;
import com.tle.beans.item.cal.request.CourseInfo;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.activation.service.ActivationService;
import com.tle.core.guice.Bind;
import com.tle.core.item.service.ItemService;
import com.tle.web.activation.operation.BulkDeactivateOperation;
import com.tle.web.activation.operation.BulkDeleteOperation;
import com.tle.web.activation.operation.BulkRolloverOperation;
import com.tle.web.bulk.operation.BulkOperationExtension;
import com.tle.web.bulk.operation.BulkOperationExtension.OperationInfo;
import com.tle.web.bulk.section.AbstractBulkResultsDialog;
import com.tle.web.bulk.section.AbstractBulkSelectionSection;
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
import com.tle.web.sections.standard.model.Option;

@Bind
@NonNullByDefault
public class ActivationResultsDialog extends AbstractBulkResultsDialog<ItemActivationId>
{
	@PlugKey("activations.opresults.count")
	private static String OPRESULTS_COUNT_KEY;

	@Inject
	private ItemService itemService;

	@Inject
	private BulkRolloverOperation rolloverOp;
	@Inject
	private BulkDeactivateOperation deactivateOp;
	@Inject
	private BulkDeleteOperation deleteOp;
	@Inject
	private ActivationService activationService;

	@TreeLookup
	private AbstractBulkSelectionSection<ItemActivationId> selectionSection;

	@EventHandlerMethod
	public void removeSelection(SectionInfo info, String uuid, int version, String activationId)
	{
		selectionSection.removeSelection(info, new ItemActivationId(uuid, version, activationId));
	}

	public class BulkOperationList extends DynamicHtmlListModel<OperationInfo>
	{
		private final List<BulkOperationExtension> bulkOps = new ArrayList<BulkOperationExtension>();

		public BulkOperationList(SectionTree tree, String parentId)
		{
			bulkOps.add(deactivateOp);
			bulkOps.add(deleteOp);
			bulkOps.add(rolloverOp);
			for( BulkOperationExtension operation : bulkOps )
			{
				operation.register(tree, parentId);
			}
		}

		@Override
		protected Iterable<Option<OperationInfo>> populateOptions(SectionInfo info)
		{
			List<Option<OperationInfo>> ops = new ArrayList<Option<OperationInfo>>();
			for( BulkOperationExtension operation : bulkOps )
			{
				operation.addOptions(info, ops);
			}
			return ops;
		}

		@Nullable
		@Override
		protected Iterable<OperationInfo> populateModel(SectionInfo info)
		{
			return null;
		}
	}

	@Override
	protected DynamicHtmlListModel<OperationInfo> getBulkOperationList(SectionTree tree, String parentId)
	{
		return new BulkOperationList(tree, parentId);
	}

	@Override
	protected List<SelectionRow> getRows(List<ItemActivationId> pageOfIds)
	{
		List<SelectionRow> rows = new ArrayList<SelectionRow>();
		for( ItemActivationId itemId : pageOfIds )
		{
			ActivateRequest request = activationService.getRequest(Long.parseLong(itemId.getActivationId()));
			Item item = itemService.get(itemId);
			CourseInfo course = request.getCourse();

			String itemName = CurrentLocale.get(item.getName(), item.getUuid());
			String attachmentName = UnmodifiableAttachments.convertToMapUuid(item.getAttachments())
				.get(request.getAttachment()).getDescription();
			String courseName = CurrentLocale.get(course.getName(), course.getUuid());

			Label description = new TextLabel(String.format("%s - %s - %s", itemName, //$NON-NLS-1$
				attachmentName, courseName));

			rows.add(new SelectionRow(description, new HtmlComponentState(RendererConstants.LINK, events
				.getNamedHandler("removeSelection", itemId.getUuid(), //$NON-NLS-1$
					itemId.getVersion(), itemId.getActivationId()))));
		}
		return rows;
	}

	@Override
	protected Label getOpResultCountLabel(int totalSelections)
	{
		return new PluralKeyLabel(OPRESULTS_COUNT_KEY, totalSelections);
	}
}
