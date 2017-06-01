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

package com.tle.web.workflow.tasks;

import java.util.List;

import com.tle.common.searching.Search.SortType;
import com.tle.common.searching.SortField;
import com.tle.common.searching.SortField.Type;
import com.tle.core.workflow.freetext.TasksIndexer;
import com.tle.web.search.event.FreetextSearchEvent;
import com.tle.web.search.sort.AbstractSortOptionsSection;
import com.tle.web.search.sort.SortOption;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.render.Label;

@SuppressWarnings("nls")
public class TaskSortSection extends AbstractSortOptionsSection<FreetextSearchEvent>
{
	private static final SortField DUEDATE_SORT = new SortField(TasksIndexer.FIELD_DUEDATE, false, Type.LONG);
	private static final String VAL_PRIORITY = "priority";
	private static final String VAL_DUEDATE = "duedate";
	private static final String VAL_WAITING = "waiting";
	@PlugKey("sort.priority")
	private static Label LABEL_PRIORITY;
	@PlugKey("sort.duedate")
	private static Label LABEL_DUEDATE;
	@PlugKey("sort.waiting")
	private static Label LABEL_WAITING;

	@Override
	protected void addSortOptions(List<SortOption> sorts)
	{
		sorts.add(new SortOption(LABEL_PRIORITY, VAL_PRIORITY, TasksIndexer.FIELD_PRIORITY, true)
		{
			@Override
			public SortField[] createSort()
			{
				return new SortField[]{field.clone(), DUEDATE_SORT.clone()};
			}
		});
		sorts.add(new SortOption(LABEL_DUEDATE, VAL_DUEDATE, DUEDATE_SORT));
		sorts.add(new SortOption(LABEL_WAITING, VAL_WAITING,
			new SortField(TasksIndexer.FIELD_STARTED, false, Type.LONG)));
		sorts.add(new SortOption(SortType.NAME));
	}

	@Override
	protected String getDefaultSearch(SectionInfo info)
	{
		return VAL_PRIORITY;
	}

}
