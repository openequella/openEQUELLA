package com.tle.web.workflow.soap;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.core.guice.Bind;
import com.tle.web.workflow.portal.TaskListFilters;

@Bind
@Singleton
public class TaskListSoapService implements TaskListSoapInterface
{
	@Inject
	private TaskListFilters taskListFilters;

	@Override
	public String getTaskFilterCounts(boolean ignoreZero)
	{
		return taskListFilters.getFilterCountsXML(ignoreZero);
	}

	@Override
	public String[] getTaskFilterNames()
	{
		return taskListFilters.getFilterNames();
	}

	@Override
	public String getTaskList(String filterName, int start, int numResults) throws Exception
	{
		return taskListFilters.getFilterResultsXML(filterName, start, numResults);
	}

}
