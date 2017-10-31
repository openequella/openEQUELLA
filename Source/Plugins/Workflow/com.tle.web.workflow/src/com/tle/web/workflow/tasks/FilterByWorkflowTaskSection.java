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

package com.tle.web.workflow.tasks;

import javax.inject.Inject;

import com.dytech.edge.common.Constants;
import com.dytech.edge.queries.FreeTextQuery;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.common.Check;
import com.tle.common.workflow.Workflow;
import com.tle.common.workflow.node.WorkflowItem;
import com.tle.common.workflow.node.WorkflowNode;
import com.tle.core.guice.Bind;
import com.tle.core.i18n.BundleCache;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.search.base.AbstractSearchResultsSection;
import com.tle.web.search.event.FreetextSearchEvent;
import com.tle.web.search.filter.ResetFiltersListener;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.search.SearchResultsActionsSection;
import com.tle.web.sections.equella.search.event.SearchEventListener;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.result.util.BundleLabel;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.DynamicHtmlListModel;
import com.tle.web.sections.standard.model.LabelOption;
import com.tle.web.sections.standard.model.Option;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@NonNullByDefault
@Bind
public class FilterByWorkflowTaskSection extends AbstractPrototypeSection<FilterByWorkflowTaskSection.Model>
	implements
		HtmlRenderer,
		SearchEventListener<FreetextSearchEvent>,
		ResetFiltersListener
{
	@Inject
	private BundleCache bundleCache;

	@ViewFactory
	private FreemarkerFactory viewFactory;
	@EventFactory
	private EventGenerator events;
	@TreeLookup
	private AbstractSearchResultsSection<?, ?, ?, ?> searchResults;
	@TreeLookup
	private WorkflowSelection workflowSelection;
	@PlugKey("filter.byworkflowtask.all")
	private static Label LABEL_ALL;

	@Component(name = "s", parameter = "task", supported = true)
	private SingleSelectionList<WorkflowNode> taskList;
	@Nullable
	private JSHandler changeHandler;

	private boolean isMyTasks;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		taskList.setListModel(new TaskListModel());
		tree.setLayout(id, SearchResultsActionsSection.AREA_FILTER);
	}

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		if( changeHandler == null )
		{
			changeHandler = searchResults.getRestartSearchHandler(tree);
		}
		taskList.addChangeEventHandler(changeHandler);
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		if( hasWorkflowTasks(context) )
		{
			return viewFactory.createResult("filterbyworkflowtask.ftl", context); //$NON-NLS-1$
		}
		return null;
	}

	@Override
	public void prepareSearch(SectionInfo info, FreetextSearchEvent event) throws Exception
	{
		if( hasWorkflowTasks(info) )
		{
			WorkflowNode value = taskList.getSelectedValue(info);
			if( value != null && !Check.isEmpty(value.getUuid()) )
			{
				event.filterByTerm(false, FreeTextQuery.FIELD_WORKFLOW_TASKID, value.getUuid());
			}
		}
		else
		{
			taskList.setSelectedStringValue(info, Constants.BLANK);
		}
	}

	public class TaskListModel extends DynamicHtmlListModel<WorkflowNode>
	{
		public TaskListModel()
		{
			setSort(true);
		}

		@Override
		protected Iterable<WorkflowNode> populateModel(SectionInfo info)
		{
			Workflow workflow = getWorkflow(info);
			if (workflow != null)
			{
				Set<WorkflowNode> nodes = workflow.getNodes();
				List<WorkflowNode> interestingNodes = new ArrayList<>();
				for (WorkflowNode node : nodes)
				{
					if (node.getType() == WorkflowNode.ITEM_TYPE || (!isMyTasks && node.getType() == WorkflowNode.SCRIPT_TYPE))
					{
						interestingNodes.add(node);
					}
				}
				return interestingNodes;
			}
			return null;
		}

		@Override
		protected Option<WorkflowNode> convertToOption(SectionInfo info, WorkflowNode obj)
		{
			return new LabelOption<WorkflowNode>(new BundleLabel(obj.getName(), bundleCache), obj.getUuid(), obj);
		}

		@Override
		protected Option<WorkflowNode> getTopOption()
		{
			return new LabelOption<WorkflowNode>(LABEL_ALL, Constants.BLANK, null);
		}
	}

	@Nullable
	private Workflow getWorkflow(SectionInfo info)
	{
		Model model = getModel(info);
		Workflow workflow = model.getWorkflow();
		if( workflow == null )
		{
			workflow = workflowSelection.getWorkflow(info);
			model.setWorkflow(workflow);
		}
		return workflow;
	}

	private boolean hasWorkflowTasks(SectionInfo info)
	{
		Workflow workflow = getWorkflow(info);
		if( workflow != null )
		{
			return !workflow.getAllWorkflowItems().values().isEmpty();
		}
		return false;
	}

	public SingleSelectionList<WorkflowNode> getTaskList()
	{
		return taskList;
	}

	@Override
	public void reset(SectionInfo info)
	{
		taskList.setSelectedStringValue(info, null);
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new Model();
	}

	@NonNullByDefault(false)
	public static class Model
	{
		private Workflow workflow;

		public Workflow getWorkflow()
		{
			return workflow;
		}

		public void setWorkflow(Workflow workflow)
		{
			this.workflow = workflow;
		}
	}

	public void setWorkflowTask(SectionInfo info, WorkflowItem task)
	{
		workflowSelection.setWorkflow(info, task.getWorkflow());
		taskList.setSelectedStringValue(info, task.getUuid());
	}

	public void setIsMyTasks(boolean isMyTasks)
	{
		this.isMyTasks = isMyTasks;
	}
}
