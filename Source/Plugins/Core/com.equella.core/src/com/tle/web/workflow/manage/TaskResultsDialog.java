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

package com.tle.web.workflow.manage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.tle.annotation.NonNullByDefault;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemTaskId;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.workflow.Workflow;
import com.tle.common.workflow.node.WorkflowNode;
import com.tle.core.guice.Bind;
import com.tle.core.item.service.ItemService;
import com.tle.core.plugins.PluginService;
import com.tle.core.plugins.PluginTracker;
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
public class TaskResultsDialog extends AbstractBulkResultsDialog<ItemTaskId>
{
	@PlugKey("tasks.opresults.count")
	private static String OPRESULTS_COUNT_KEY;

	@Inject
	private ItemService itemService;

	@TreeLookup
	private AbstractBulkSelectionSection<ItemTaskId> selectionSection;

	private PluginTracker<BulkOperationExtension> tracker;

	@Inject
	public void setPluginService(PluginService pluginService)
	{
		tracker = new PluginTracker<BulkOperationExtension>(pluginService, "com.tle.web.workflow", "bulkExtension", null)
			.setBeanKey("bean");
	}

	@EventHandlerMethod
	public void removeSelection(SectionInfo info, String id, int version, String taskId)
	{
		selectionSection.removeSelection(info, new ItemTaskId(id, version, taskId));
	}

	public class BulkOperationList extends DynamicHtmlListModel<OperationInfo>
	{
		private final List<BulkOperationExtension> bulkOps = new ArrayList<BulkOperationExtension>();

		public BulkOperationList(SectionTree tree, String parentId)
		{
			bulkOps.addAll(tracker.getNewBeanList());
			for( BulkOperationExtension op : bulkOps )
			{
				op.register(tree, parentId);
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
	protected Label getOpResultCountLabel(int totalSelections)
	{
		return new PluralKeyLabel(OPRESULTS_COUNT_KEY, totalSelections);
	}

	@Override
	protected List<SelectionRow> getRows(List<ItemTaskId> pageOfIds)
	{
		List<SelectionRow> rows = new ArrayList<SelectionRow>();
		for( ItemTaskId itemId : pageOfIds )
		{
			Item item = itemService.get(itemId);
			Workflow workflow = item.getItemDefinition().getWorkflow();
			Map<String, WorkflowNode> allWorkflowTasks = workflow.getAllWorkflowTasks();
			WorkflowNode workflowNode = allWorkflowTasks.get(itemId.getTaskId());

			String itemName = CurrentLocale.get(item.getName(), item.getUuid());
			String stepName = CurrentLocale.get(workflowNode.getName(), workflowNode.getUuid());
			Label newLabel = new TextLabel(itemName + " - " + stepName);

			rows.add(new SelectionRow(newLabel, new HtmlComponentState(RendererConstants.LINK,
				events.getNamedHandler("removeSelection", itemId.getUuid(), itemId.getVersion(), itemId.getTaskId()))));
		}
		return rows;
	}
}
