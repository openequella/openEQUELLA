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
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import com.google.common.collect.Lists;
import com.tle.beans.entity.BaseEntityLabel;
import com.tle.common.search.DefaultSearch;
import com.tle.common.workflow.Workflow;
import com.tle.core.i18n.BundleCache;
import com.tle.core.workflow.freetext.ManageTasksSearch;
import com.tle.core.workflow.service.WorkflowService;
import com.tle.web.search.base.AbstractSearchResultsSection;
import com.tle.web.search.event.FreetextSearchEvent;
import com.tle.web.search.filter.AbstractResetFiltersQuerySection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.search.SearchResultsActionsSection;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.js.generic.StatementHandler;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.Option;
import com.tle.web.workflow.tasks.WorkflowSelection;

@SuppressWarnings("nls")
public class TaskManagementQuerySection extends AbstractResetFiltersQuerySection<Object, FreetextSearchEvent>
	implements
		WorkflowSelection
{
	private static final String DIV_QUERY = "searchform";

	@Inject
	private BundleCache bundleCache;
	@Inject
	private WorkflowService workflowService;

	@Component(parameter = "inwork", supported = true)
	private SingleSelectionList<BaseEntityLabel> workflowList;

	@TreeLookup
	private AbstractSearchResultsSection<?, ?, ?, ?> searchResults;

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		return viewFactory.createResult("managequery.ftl", this);
	}

	@Override
	protected String getAjaxDiv()
	{
		return DIV_QUERY;
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		searchButton.setLabel(new KeyLabel("item.section.query.search"));
		workflowList.setListModel(new WorkflowListModel(workflowService, bundleCache));
	}

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		super.treeFinished(id, tree);
		JSHandler searchHandler = searchResults.getRestartSearchHandler(tree);
		searchButton.setClickHandler(searchHandler);
		workflowList.addChangeEventHandler(new StatementHandler(
			searchResults.getResultsUpdater(tree, null, SearchResultsActionsSection.ACTIONS_AJAX_ID)));
	}

	public SingleSelectionList<BaseEntityLabel> getWorkflowList()
	{
		return workflowList;
	}

	public DefaultSearch createSearch(SectionInfo info)
	{
		Collection<String> workflowUuids;
		BaseEntityLabel selectedValue = workflowList.getSelectedValue(info);
		if( selectedValue != null )
		{
			workflowUuids = Collections.singleton(selectedValue.getUuid());
		}
		else
		{
			workflowUuids = Lists.newArrayList();
			List<Option<BaseEntityLabel>> options = workflowList.getListModel().getOptions(info);
			for( Option<BaseEntityLabel> option : options )
			{
				BaseEntityLabel workflow = option.getObject();
				if( workflow != null )
				{
					workflowUuids.add(workflow.getUuid());
				}
			}
			if( workflowUuids.isEmpty() )
			{
				// If you don't have access to any, you need to filter by
				// something impossible
				workflowUuids.add("*");
			}
		}
		return new ManageTasksSearch(workflowUuids);
	}

	@Override
	public Workflow getWorkflow(SectionInfo info)
	{
		BaseEntityLabel val = workflowList.getSelectedValue(info);
		if( val != null )
		{
			return workflowService.getByUuid(val.getUuid());
		}
		return null;
	}

	@Override
	public void setWorkflow(SectionInfo info, Workflow workflow)
	{
		workflowList.setSelectedStringValue(info, workflow.getUuid());
	}
}
