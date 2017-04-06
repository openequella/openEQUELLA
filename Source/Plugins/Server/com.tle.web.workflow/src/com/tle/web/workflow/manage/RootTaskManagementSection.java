package com.tle.web.workflow.manage;

import com.tle.web.search.base.ContextableSearchSection;
import com.tle.web.sections.InfoCreator;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.layout.ContentLayout;
import com.tle.web.sections.render.Label;

public class RootTaskManagementSection extends ContextableSearchSection<ContextableSearchSection.Model>
{
	@PlugKey("manage.title")
	private static Label LABEL_TITLE;

	@SuppressWarnings("nls")
	@Override
	protected String getSessionKey()
	{
		return "$MANAGE_KEY$";
	}

	@Override
	public Label getTitle(SectionInfo info)
	{
		return LABEL_TITLE;
	}

	@SuppressWarnings("nls")
	public static SectionInfo create(InfoCreator creator)
	{
		return creator.createForward("/access/managetasks.do");
	}

	@Override
	protected ContentLayout getDefaultLayout(SectionInfo info)
	{
		return ContentLayout.ONE_COLUMN;
	}
}
