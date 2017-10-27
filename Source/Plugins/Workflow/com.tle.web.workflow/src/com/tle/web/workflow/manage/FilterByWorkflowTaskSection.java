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

import java.util.Collection;

import javax.inject.Inject;

import com.dytech.edge.common.Constants;
import com.dytech.edge.queries.FreeTextQuery;
import com.tle.beans.entity.BaseEntityLabel;
import com.tle.common.workflow.Workflow;
import com.tle.common.workflow.node.WorkflowNode;
import com.tle.core.guice.Bind;
import com.tle.core.i18n.BundleCache;
import com.tle.core.workflow.service.WorkflowService;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.itemadmin.section.ItemAdminFilterByItemStatusSection;
import com.tle.web.itemadmin.section.ItemAdminQuerySection;
import com.tle.web.search.base.AbstractSearchResultsSection;
import com.tle.web.search.event.FreetextSearchEvent;
import com.tle.web.search.filter.ResetFiltersListener;
import com.tle.web.search.sort.AbstractSortOptionsSection;
import com.tle.web.search.sort.SortOption;
import com.tle.web.search.sort.SortOptionsListener;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.search.SearchResultsActionsSection;
import com.tle.web.sections.equella.search.event.SearchEventListener;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.result.util.BundleLabel;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.DynamicHtmlListModel;
import com.tle.web.sections.standard.model.LabelOption;
import com.tle.web.sections.standard.model.Option;

@Bind
public class FilterByWorkflowTaskSection extends AbstractPrototypeSection<Object>
	implements
		HtmlRenderer,
		SearchEventListener<FreetextSearchEvent>,
		ResetFiltersListener,
		SortOptionsListener
{
	@PlugKey("sort.workflow.inmod")
	private static Label LABEL_TIMEINMOD;

	@ViewFactory
	private FreemarkerFactory viewFactory;

	@Inject
	private BundleCache bundleCache;

	@Inject
	private WorkflowService workflowService;

	@TreeLookup
	private AbstractSearchResultsSection<?, ?, ?, ?> searchResults;

	@TreeLookup
	private FilterByWorkflowSection filterByWorkflowSection;

	@TreeLookup
	private ItemAdminFilterByItemStatusSection itemStatus;
	@TreeLookup
	private ItemAdminQuerySection itemAdminQuery;

	@Component(parameter = "workflowTask", supported = true)
	private SingleSelectionList<WorkflowNode> taskList;

	@PlugKey("filter.byworkflowtask.all")
	private static Label LABEL_ALL;

	@SuppressWarnings("nls")
	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		getTaskList().setListModel(new TaskListModel());
		tree.setLayout(id, SearchResultsActionsSection.AREA_FILTER);
	}

	private Workflow getWorkflow(SectionInfo info)
	{
		BaseEntityLabel selectedValue = filterByWorkflowSection.getWorkflowList().getSelectedValue(info);
		if( selectedValue != null )
		{
			Workflow workflow = workflowService.getByUuid(selectedValue.getUuid());
			return workflow;
		}
		return null;
	}

	private boolean hasWorkflowTasks(SectionInfo info)
	{
		Workflow workflow = getWorkflow(info);

		if( workflow != null )
		{
			return !workflow.getAllWorkflowTasks().values().isEmpty();
		}
		return false;
	}

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		super.treeFinished(id, tree);

		getTaskList().addChangeEventHandler(searchResults.getRestartSearchHandler(tree));
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		if( !isShowing(context) )
		{
			return null;
		}
		return viewFactory.createResult("tasklist.ftl", context); //$NON-NLS-1$
	}

	private boolean isShowing(SectionInfo info)
	{
		boolean workFlowSelected = (filterByWorkflowSection.getWorkflowList().getSelectedValue(info) != null);
		return filterByWorkflowSection.isShowing(info) && workFlowSelected && hasWorkflowTasks(info);
	}

	@Override
	public void prepareSearch(SectionInfo info, FreetextSearchEvent event) throws Exception
	{
		if( isShowing(info) )
		{
			WorkflowNode selectedValue = taskList.getSelectedValue(info);
			if( selectedValue != null )
			{
				event.filterByTerm(false, FreeTextQuery.FIELD_WORKFLOW_TASKID, selectedValue.getUuid());
			}
		}
	}

	@Override
	public void reset(SectionInfo info)
	{
		taskList.setSelectedStringValue(info, null);
	}

	@Override
	public Iterable<SortOption> addSortOptions(SectionInfo info, AbstractSortOptionsSection<?> section)
	{
		return null;
	}

	public SingleSelectionList<WorkflowNode> getTaskList()
	{
		return taskList;
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
			String uuid = filterByWorkflowSection.getWorkflowUuid(info);

			if( uuid != null )
			{
				Workflow workflow = workflowService.getByUuid(uuid);
				if( workflow != null )
				{
					Collection<WorkflowNode> values = workflow.getAllWorkflowTasks().values();
					return values;
				}
				return null;
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
}
