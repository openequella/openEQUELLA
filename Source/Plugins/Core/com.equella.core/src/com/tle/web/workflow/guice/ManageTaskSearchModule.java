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
import com.tle.web.search.filter.FilterByDateRangeSection;
import com.tle.web.search.filter.FilterByItemStatusTaskSection;
import com.tle.web.search.filter.FilterByOwnerSection;
import com.tle.web.search.guice.AbstractSearchModule;
import com.tle.web.workflow.manage.FilterByModeratorSection;
import com.tle.web.workflow.manage.RootTaskManagementSection;
import com.tle.web.workflow.manage.TaskManagementQuerySection;
import com.tle.web.workflow.manage.TaskManagementResultsSection;
import com.tle.web.workflow.manage.TaskSelectionSection;
import com.tle.web.workflow.tasks.FilterByAssignee;
import com.tle.web.workflow.tasks.FilterByCollectionForWorkflowSection;
import com.tle.web.workflow.tasks.FilterByWorkflowTaskSection;
import com.tle.web.workflow.tasks.TaskSortSection;

@SuppressWarnings("nls")
public class ManageTaskSearchModule extends AbstractSearchModule
{
	@Override
	protected NodeProvider getRootNode()
	{
		return node(RootTaskManagementSection.class);
	}

	@Override
	protected NodeProvider getQueryNode()
	{
		return node(TaskManagementQuerySection.class);
	}

	@Override
	protected NodeProvider getResultsNode()
	{
		return node(TaskManagementResultsSection.class);
	}

	@Override
	protected String getTreeName()
	{
		return "/access/managetasks";
	}

	@Override
	protected void addSearchResultsActions(NodeProvider node)
	{
		node.child(RssFeedSection.class);
		node.child(TaskSortSection.class);
		node.child(FilterByWorkflowTaskSection.class);
		node.child(FilterByModeratorSection.class);
		node.child(FilterByAssignee.class);
		node.child(FilterByCollectionForWorkflowSection.class);
		node.child(FilterByOwnerSection.class);
		node.child(FilterByDateRangeSection.class);
		node.child(FilterByItemStatusTaskSection.class);
	}

	@Override
	protected void addActions(NodeProvider node)
	{
		node.child(TaskSelectionSection.class);
	}
}
