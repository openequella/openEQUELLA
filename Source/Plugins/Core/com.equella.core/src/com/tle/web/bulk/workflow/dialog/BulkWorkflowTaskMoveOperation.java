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

package com.tle.web.bulk.workflow.dialog;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import com.google.inject.assistedinject.Assisted;
import com.tle.beans.item.ItemPack;
import com.tle.common.Check;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.workflow.Workflow;
import com.tle.core.guice.Bind;
import com.tle.core.guice.BindFactory;
import com.tle.core.item.operations.WorkflowOperation;
import com.tle.core.item.standard.ItemOperationFactory;
import com.tle.core.plugins.BeanLocator;
import com.tle.core.plugins.FactoryMethodLocator;
import com.tle.core.workflow.service.WorkflowService;
import com.tle.web.bulk.operation.BulkOperationExecutor;
import com.tle.web.bulk.operation.BulkOperationExtension;
import com.tle.web.bulk.workflow.BulkWorkflowOperationFactory;
import com.tle.web.bulk.workflow.section.BulkWorkflowTaskMoveSection;
import com.tle.web.itemadmin.section.ItemAdminFilterByItemStatusSection;
import com.tle.web.itemadmin.section.ItemAdminResultsDialog;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.utils.KeyOption;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.model.Option;
import com.tle.web.workflow.manage.FilterByWorkflowSection;

@Bind
public class BulkWorkflowTaskMoveOperation extends BulkWorkflowTaskMoveSection implements BulkOperationExtension
{
	private static final String BULK_MOVETASK_VAL = "movetasks";

	@PlugKey("bulkop.movetask")
	private static String STRING_MOVETASK;
	@PlugKey("bulkop.movetask.title")
	private static Label LABEL_MOVETASK_TITLE;

	@Inject
	private WorkflowService workflowService;

	@TreeLookup
	private ItemAdminFilterByItemStatusSection itemStatus;
	@TreeLookup
	private FilterByWorkflowSection filterByWorkflowSection;
	@TreeLookup
	private ItemAdminResultsDialog itemAdminResultsDialog;

	@Override
	public void register(SectionTree tree, String parentId)
	{
		tree.registerInnerSection(this, parentId);
	}

	private boolean isShowing(SectionInfo info)
	{
		String workflowId = getSelectedWorkflowId(info);
		BulkWorkflowTaskMoveModel model = getModel(info);
		if( !Check.isEmpty(workflowId) )
		{
			Workflow selectedWorkflow = workflowService.getByUuid(workflowId);
			model.setWorkflow(selectedWorkflow);
			model.setWorkflowName(CurrentLocale.get(selectedWorkflow.getName()));
		}
		return itemStatus.getOnlyInModeration().isChecked(info) && !Check.isEmpty(workflowId);
	}

	private String getSelectedWorkflowId(SectionInfo info)
	{
		return filterByWorkflowSection.getWorkflowList().getSelectedValueAsString(info);
	}

	@Override
	public void addOptions(SectionInfo info, List<Option<OperationInfo>> options)
	{
		if( isShowing(info) )
		{
			options.add(new KeyOption<OperationInfo>(STRING_MOVETASK, BULK_MOVETASK_VAL,
				new OperationInfo(this, BULK_MOVETASK_VAL)));
		}
	}

	@BindFactory
	public interface MoveOperationExecutorFactory
	{
		WorkflowTaskMoveExecutor move(@Assisted("msg") String msg, @Assisted("toStep") String toStep);
	}

	@Override
	public BeanLocator<WorkflowTaskMoveExecutor> getExecutor(SectionInfo info, String operationId)
	{
		return new FactoryMethodLocator<WorkflowTaskMoveExecutor>(MoveOperationExecutorFactory.class, "move",
			getComment(info), getSelectedWorkflowNode(info));
	}

	public static class WorkflowTaskMoveExecutor implements BulkOperationExecutor
	{
		private static final long serialVersionUID = 1L;
		private final String msg;
		private final String toStep;

		@Inject
		public WorkflowTaskMoveExecutor(@Assisted("msg") String msg, @Assisted("toStep") String toStep)
		{
			this.msg = msg;
			this.toStep = toStep;
		}

		@Inject
		private ItemOperationFactory workflowFactory;
		@Inject
		private BulkWorkflowOperationFactory bulkWorkflowOpFactory;

		@Override
		public WorkflowOperation[] getOperations()
		{
			return new WorkflowOperation[]{bulkWorkflowOpFactory.taskMove(msg, toStep), workflowFactory.save()};
		}

		@Override
		public String getTitleKey()
		{
			return "com.tle.web.bulk.workflow.bulk.movetask.title";
		}

	}

	@Override
	public void prepareDefaultOptions(SectionInfo info, String operationId)
	{
		// none
	}

	@Override
	public SectionRenderable renderOptions(RenderContext context, String operationId)
	{
		return renderSection(context, this);
	}

	@Override
	public Label getStatusTitleLabel(SectionInfo info, String operationId)
	{
		return LABEL_MOVETASK_TITLE;
	}

	@Override
	public boolean validateOptions(SectionInfo info, String operationId)
	{
		return true;
	}

	@Override
	public boolean areOptionsFinished(SectionInfo info, String operationId)
	{
		return itemAdminResultsDialog.getModel(info).isShowOptions();
	}

	@Override
	public boolean hasExtraOptions(SectionInfo info, String operationId)
	{
		return true;
	}

	@Override
	public boolean hasExtraNavigation(SectionInfo info, String operationId)
	{
		return false;
	}

	@Override
	public Collection<Button> getExtraNavigation(SectionInfo info, String operationId)
	{
		return null;
	}

	@Override
	public boolean hasPreview(SectionInfo info, String operationId)
	{
		return false;
	}

	@Override
	public ItemPack runPreview(SectionInfo info, String operationId, long itemId) throws Exception
	{
		return null;
	}

	@Override
	public boolean showPreviousButton(SectionInfo info, String opererationId)
	{
		return true;
	}

}
