package com.tle.web.viewitem.summary.sidebar.actions;

import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.viewitem.summary.section.ItemSummaryContentSection;

public abstract class GenericMinorActionWithPageSection extends GenericMinorActionSection
{
	@TreeLookup
	private ItemSummaryContentSection contentSection;

	@Override
	public void execute(SectionInfo info)
	{
		contentSection.setSummaryId(info, getPageSection());
	}

	protected abstract SectionId getPageSection();
}
