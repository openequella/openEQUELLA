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
