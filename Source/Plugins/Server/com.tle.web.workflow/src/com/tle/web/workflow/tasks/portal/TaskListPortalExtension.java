package com.tle.web.workflow.tasks.portal;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.tle.core.guice.Bind;
import com.tle.web.workflow.portal.TaskListExtension;
import com.tle.web.workflow.portal.TaskListSubsearch;
import com.tle.web.workflow.tasks.FilterByAssignment.Assign;
import com.tle.web.workflow.tasks.portal.TaskListFilter.TaskListFilterFactory;

@Bind
@Singleton
public class TaskListPortalExtension implements TaskListExtension
{
	@Inject
	private TaskListFilterFactory filterFactory;

	@SuppressWarnings("nls")
	@Override
	public List<TaskListSubsearch> getTaskFilters()
	{
		Builder<TaskListSubsearch> taskFilters = ImmutableList.builder();
		taskFilters.add(filterFactory.create("taskall", Assign.ANY, false, false));
		taskFilters.add(filterFactory.create("taskme", Assign.ME, false, true));
		taskFilters.add(filterFactory.create("taskothers", Assign.OTHERS, false, true));
		taskFilters.add(filterFactory.create("tasknoone", Assign.NOONE, false, true));
		taskFilters.add(filterFactory.create("taskmust", Assign.ANY, true, true));
		return taskFilters.build();
	}
}
