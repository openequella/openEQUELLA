package com.tle.web.workflow.guice;

import com.tle.web.notification.section.RssFeedSection;
import com.tle.web.search.filter.FilterByCollectionSection;
import com.tle.web.search.filter.FilterByDateRangeSection;
import com.tle.web.search.filter.FilterByItemStatusTaskSection;
import com.tle.web.search.filter.FilterByMimeTypeSection;
import com.tle.web.search.filter.FilterByOwnerSection;
import com.tle.web.search.filter.SimpleResetFiltersQuerySection;
import com.tle.web.search.guice.AbstractSearchModule;
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
		node.child(FilterByWorkflowTaskSection.class);
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
