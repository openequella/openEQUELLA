package com.tle.web.workflow.view;

import com.tle.beans.workflow.WorkflowStatus;
import com.tle.core.guice.Bind;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.render.Label;
import com.tle.web.viewitem.summary.sidebar.actions.GenericMinorActionWithPageSection;
import com.tle.web.viewurl.ItemSectionInfo;

@Bind
public class CurrentModerationLinkSection extends GenericMinorActionWithPageSection
{
	@PlugKey("summary.sidebar.summary.currentmoderation.title")
	private static Label LINK_LABEL;

	@TreeLookup
	private CurrentModerationContentSection currentModerationContentSection;

	public CurrentModerationLinkSection()
	{
		setShowForPreview(true);
	}

	@Override
	protected Label getLinkLabel()
	{
		return LINK_LABEL;
	}

	@Override
	protected boolean canView(SectionInfo info, ItemSectionInfo itemInfo, WorkflowStatus status)
	{
		return status.isModerating();
	}

	@Override
	protected SectionId getPageSection()
	{
		return currentModerationContentSection;
	}

	@Override
	public String getLinkText()
	{
		return LINK_LABEL.getText();
	}
}
