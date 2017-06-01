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

import javax.inject.Inject;

import com.tle.common.search.DefaultSearch;
import com.tle.core.workflow.freetext.TaskListSearch;
import com.tle.web.search.base.AbstractFreetextResultsSection;
import com.tle.web.search.base.AbstractSearchResultsSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.workflow.tasks.TaskItemList.TaskItemListEntry;

public class TaskListResultsSection
	extends
		AbstractFreetextResultsSection<TaskItemListEntry, AbstractSearchResultsSection.SearchResultsModel>
{
	@Inject
	private TaskItemList taskItemList;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
	}

	@Override
	protected void registerItemList(SectionTree tree, String id)
	{
		tree.registerInnerSection(taskItemList, id);
	}

	@Override
	public TaskItemList getItemList(SectionInfo info)
	{
		return taskItemList;
	}

	@Override
	protected DefaultSearch createDefaultSearch(SectionInfo info)
	{
		return new TaskListSearch();
	}

}
