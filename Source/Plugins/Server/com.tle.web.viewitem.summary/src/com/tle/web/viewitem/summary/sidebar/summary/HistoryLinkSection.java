package com.tle.web.viewitem.summary.sidebar.summary;

import com.tle.beans.workflow.WorkflowStatus;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.render.Label;
import com.tle.web.viewitem.summary.content.HistoryContentSection;
import com.tle.web.viewitem.summary.sidebar.actions.GenericMinorActionWithPageSection;
import com.tle.web.viewurl.ItemSectionInfo;

public class HistoryLinkSection extends GenericMinorActionWithPageSection
{
	@PlugKey("summary.sidebar.summary.history.title")
	private static Label LINK_LABEL;

	@TreeLookup
	private HistoryContentSection historyContentSection;

	public HistoryLinkSection()
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
		return getItemInfo(info).hasPrivilege(HistoryContentSection.VIEW_PRIVILEGE);
	}

	@Override
	protected SectionId getPageSection()
	{
		return historyContentSection;
	}

	@Override
	public String getLinkText()
	{
		return LINK_LABEL.getText();
	}
}
