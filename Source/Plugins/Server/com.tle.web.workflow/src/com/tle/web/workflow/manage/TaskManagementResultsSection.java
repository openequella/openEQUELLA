package com.tle.web.workflow.manage;

import javax.inject.Inject;

import com.tle.common.search.DefaultSearch;
import com.tle.web.search.base.AbstractFreetextResultsSection;
import com.tle.web.search.base.AbstractSearchResultsSection.SearchResultsModel;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.workflow.manage.TaskManagementItemList.TaskManagementListEntry;

public class TaskManagementResultsSection
	extends
		AbstractFreetextResultsSection<TaskManagementListEntry, SearchResultsModel>
{

	@Inject
	private TaskManagementItemList taskItemList;
	@TreeLookup
	private TaskManagementQuerySection querySection;

	@Override
	protected void registerItemList(SectionTree tree, String id)
	{
		tree.registerInnerSection(taskItemList, id);
	}

	@Override
	public TaskManagementItemList getItemList(SectionInfo info)
	{
		return taskItemList;
	}

	@Override
	protected DefaultSearch createDefaultSearch(SectionInfo info)
	{
		return querySection.createSearch(info);
	}

}
