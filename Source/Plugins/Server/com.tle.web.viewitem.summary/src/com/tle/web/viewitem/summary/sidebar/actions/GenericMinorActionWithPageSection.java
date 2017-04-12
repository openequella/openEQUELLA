package com.tle.web.viewitem.summary.sidebar.actions;

import com.tle.annotation.NonNullByDefault;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.render.HideableFromDRMSection;
import com.tle.web.viewitem.summary.section.ItemSummaryContentSection;

@NonNullByDefault
public abstract class GenericMinorActionWithPageSection extends GenericMinorActionSection
	implements
		HideableFromDRMSection
{
	@TreeLookup
	private ItemSummaryContentSection contentSection;

	@Override
	public void execute(SectionInfo info)
	{
		contentSection.setSummaryId(info, getPageSection());
	}

	@Override
	public void showSection(SectionInfo info, boolean show)
	{
		getComponent().setDisplayed(show);
	}

	protected abstract SectionId getPageSection();
}
