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

package com.tle.web.workflow.guice;

import com.tle.web.notification.section.RssFeedSection;
import com.tle.web.search.filter.FilterByCollectionSection;
import com.tle.web.search.filter.FilterByDateRangeSection;
import com.tle.web.search.filter.FilterByItemStatusTaskSection;
import com.tle.web.search.filter.FilterByMimeTypeSection;
import com.tle.web.search.filter.FilterByOwnerSection;
import com.tle.web.search.filter.SimpleResetFiltersQuerySection;
import com.tle.web.search.guice.AbstractSearchModule;
import com.tle.web.sections.Section;
import com.tle.web.workflow.tasks.FilterByAssignment;
import com.tle.web.workflow.tasks.FilterByWorkflowTaskSection;
import com.tle.web.workflow.tasks.ModerateSelectedButton;
import com.tle.web.workflow.tasks.RootTaskListSection;
import com.tle.web.workflow.tasks.TaskListResultsSection;
import com.tle.web.workflow.tasks.TaskSortSection;
import com.tle.web.workflow.tasks.WorkflowFromCollectionSection;

@SuppressWarnings("nls")
public class TaskSearchModule extends AbstractSearchModule
{

	@Override
	protected NodeProvider getRootNode()
	{
		return node(RootTaskListSection.class);
	}

	@Override
	protected NodeProvider getQueryNode()
	{
		return node(SimpleResetFiltersQuerySection.class);
	}

	@Override
	protected NodeProvider getResultsNode()
	{
		return node(TaskListResultsSection.class);
	}

	@Override
	protected void addSearchResultsActions(NodeProvider node)
	{
		node.child(RssFeedSection.class);
		node.child(TaskSortSection.class);
		node.child(FilterByAssignment.class);
		node.child(FilterByCollectionSection.class);
		node.child(WorkflowFromCollectionSection.class);
		node.child(new NodeProvider(FilterByWorkflowTaskSection.class)
			{
				@Override
				protected void customize(Section section)
				{
					FilterByWorkflowTaskSection s = (FilterByWorkflowTaskSection) section;
					s.setIsMyTasks(true);
				}
			}
		);
		node.child(FilterByOwnerSection.class);
		node.child(FilterByDateRangeSection.class);
		node.child(FilterByMimeTypeSection.class);
		node.child(FilterByItemStatusTaskSection.class);
	}

	@Override
	protected String getTreeName()
	{
		return "/access/tasklist";
	}

	@Override
	protected void addActions(NodeProvider node)
	{
		node.child(ModerateSelectedButton.class);
	}
}
