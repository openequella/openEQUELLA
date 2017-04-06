package com.tle.core.workflow.freetext;

import java.util.Collection;
import java.util.List;

import com.tle.common.search.DefaultSearch;
import com.tle.common.searching.Field;
import com.tle.common.searching.Search;

public class ManageTasksSearch extends DefaultSearch
{
	private static final long serialVersionUID = 1L;
	private Collection<String> uuids;

	public ManageTasksSearch(Collection<String> uuids)
	{
		this.uuids = uuids;
	}

	@Override
	public String getPrivilege()
	{
		return null;
	}

	@Override
	public String getSearchType()
	{
		return Search.INDEX_TASK;
	}

	@Override
	protected void addExtraMusts(List<List<Field>> musts)
	{
		musts.add(createFields(TasksIndexer.FIELD_WORKFLOW, uuids));
	}
}
