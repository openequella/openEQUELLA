package com.tle.web.workflow.guice;

import com.tle.web.notification.section.RssFeedSection;
import com.tle.web.search.guice.AbstractSearchModule;
import com.tle.web.workflow.manage.FilterByModeratorSection;
import com.tle.web.workflow.manage.RootTaskManagementSection;
import com.tle.web.workflow.manage.TaskManagementQuerySection;
import com.tle.web.workflow.manage.TaskManagementResultsSection;
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
	}
}
