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

package com.tle.core.workflow.freetext;

import java.util.List;

import com.dytech.edge.queries.FreeTextQuery;
import com.tle.common.search.DefaultSearch;
import com.tle.common.searching.Field;
import com.tle.common.searching.Search;
import com.tle.common.usermanagement.user.CurrentUser;

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
