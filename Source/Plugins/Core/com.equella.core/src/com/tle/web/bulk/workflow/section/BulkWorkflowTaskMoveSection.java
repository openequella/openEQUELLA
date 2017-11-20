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

package com.tle.web.bulk.workflow.section;

import javax.inject.Inject;

import com.tle.annotation.NonNullByDefault;
import com.tle.common.workflow.Workflow;
import com.tle.common.workflow.node.WorkflowNode;
import com.tle.core.guice.Bind;
import com.tle.core.i18n.BundleCache;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.result.util.BundleLabel;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.DynamicHtmlListModel;
import com.tle.web.sections.standard.model.LabelOption;
import com.tle.web.sections.standard.model.Option;

@NonNullByDefault
@Bind
public class BulkWorkflowTaskMoveSection
	extends
		AbstractPrototypeSection<BulkWorkflowTaskMoveSection.BulkWorkflowTaskMoveModel>
	implements HtmlRenderer
{
	@Inject
	private BundleCache bundleCache;

	@Component(name = "s", parameter = "task", supported = true)
	private SingleSelectionList<WorkflowNode> taskList;

	@Component
	private TextField commentField;

	@ViewFactory
	private FreemarkerFactory viewFactory;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		taskList.setListModel(new TaskListModel());
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		return viewFactory.createResult("taskselection.ftl", context);
	}

	public class TaskListModel extends DynamicHtmlListModel<WorkflowNode>
	{
		@Override
		protected Iterable<WorkflowNode> populateModel(SectionInfo info)
		{
			Workflow workflow = getModel(info).getWorkflow();
			return workflow != null ? workflow.getAllWorkflowTasks(workflow.getRoot()).values() : null;
		}

		@Override
		protected Option<WorkflowNode> convertToOption(SectionInfo info, WorkflowNode obj)
		{
			return new LabelOption<WorkflowNode>(new BundleLabel(obj.getName(), bundleCache), obj.getUuid(), obj);
		}
	}

	@Override
	public Class<BulkWorkflowTaskMoveModel> getModelClass()
	{
		return BulkWorkflowTaskMoveModel.class;
	}

	public SingleSelectionList<WorkflowNode> getTaskList()
	{
		return taskList;
	}

	public String getSelectedWorkflowNode(SectionInfo info)
	{
		return taskList.getSelectedValueAsString(info);
	}

	public TextField getCommentField()
	{
		return commentField;
	}

	public String getComment(SectionInfo info)
	{
		return commentField.getValue(info);
	}

	public static class BulkWorkflowTaskMoveModel
	{
		private Workflow workflow;
		private String workflowName;

		public Workflow getWorkflow()
		{
			return workflow;
		}

		public void setWorkflow(Workflow workflow)
		{
			this.workflow = workflow;
		}

		public String getWorkflowName()
		{
			return workflowName;
		}

		public void setWorkflowName(String workflowName)
		{
			this.workflowName = workflowName;
		}
	}
}
