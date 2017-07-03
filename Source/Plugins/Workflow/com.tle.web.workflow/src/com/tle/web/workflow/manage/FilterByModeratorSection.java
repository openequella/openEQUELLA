package com.tle.web.workflow.manage;

import com.tle.common.Check;
import com.tle.core.workflow.freetext.TasksIndexer;
import com.tle.web.search.event.FreetextSearchEvent;
import com.tle.web.search.filter.AbstractFilterByUserSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.render.Label;

public class FilterByModeratorSection extends AbstractFilterByUserSection<FreetextSearchEvent>
{
	@PlugKey("filter.mod.title")
	private static Label LABEL_TITLE;
	@PlugKey("filter.mod.dialog")
	private static Label LABEL_DIALOG_TITLE;

	@Override
	public void prepareSearch(SectionInfo info, FreetextSearchEvent event) throws Exception
	{
		String userId = getSelectedUserId(info);
		if( !Check.isEmpty(userId) )
		{
			event.filterByTerm(false, TasksIndexer.FIELD_MODUSER, userId);
		}
	}

	@Override
	protected String getPublicParam()
	{
		return "mod";
	}

	@Override
	public Label getTitle()
	{
		return LABEL_TITLE;
	}

	@Override
	public Label getDialogTitle()
	{
		return LABEL_DIALOG_TITLE;
	}

	@Override
	public String getAjaxDiv()
	{
		return "moderator";
	}
}
