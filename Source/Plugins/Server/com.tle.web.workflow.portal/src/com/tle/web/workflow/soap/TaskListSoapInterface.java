package com.tle.web.workflow.soap;

public interface TaskListSoapInterface
{
	String getTaskFilterCounts(boolean ignoreZero);

	String[] getTaskFilterNames();

	String getTaskList(String filterName, int start, int numResults) throws Exception;
}
