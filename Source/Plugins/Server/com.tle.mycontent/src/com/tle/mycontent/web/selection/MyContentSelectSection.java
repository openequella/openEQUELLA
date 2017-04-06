package com.tle.mycontent.web.selection;

import com.tle.web.search.base.ContextableSearchSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.render.Label;

public class MyContentSelectSection extends ContextableSearchSection<ContextableSearchSection.Model>
{
	@PlugKey("select.title")
	private static Label LABEL_TITLE;

	@SuppressWarnings("nls")
	@Override
	protected String getSessionKey()
	{
		return "myContentSelect";
	}

	@Override
	public Label getTitle(SectionInfo info)
	{
		return LABEL_TITLE;
	}
}
