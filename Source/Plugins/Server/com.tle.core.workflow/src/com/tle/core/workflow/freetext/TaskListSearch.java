package com.tle.core.workflow.freetext;

import java.util.List;

import com.dytech.edge.queries.FreeTextQuery;
import com.tle.common.search.DefaultSearch;
import com.tle.common.searching.Field;
import com.tle.common.searching.Search;
import com.tle.core.user.CurrentUser;

public class TaskListSearch extends DefaultSearch
{
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("nls")
	@Override
	public String getPrivilege()
	{
		return "MODERATE_ITEM";
	}

	@Override
	public String getSearchType()
	{
		return Search.INDEX_TASK;
	}

	@Override
	protected void addExtraMustNots(List<List<Field>> mustNots)
	{
		mustNots.add(createFields(FreeTextQuery.FIELD_WORKFLOW_ACCEPTED, CurrentUser.getUserID()));
	}
}
