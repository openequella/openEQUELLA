package com.tle.web.viewitem.summary.sidebar.summary;

import com.tle.beans.workflow.WorkflowStatus;
import com.tle.core.guice.Bind;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.render.Label;
import com.tle.web.viewitem.summary.content.ExportContentSection;
import com.tle.web.viewitem.summary.sidebar.actions.GenericMinorActionWithPageSection;
import com.tle.web.viewurl.ItemSectionInfo;

@Bind
public class ExportLinkSection extends GenericMinorActionWithPageSection
{
	@PlugKey("summary.sidebar.summary.export.title")
	private static Label LINK_LABEL;

	@TreeLookup
	private ExportContentSection exportContentSection;

	@Override
	protected Label getLinkLabel()
	{
		return LINK_LABEL;
	}

	@Override
	protected boolean canView(SectionInfo info, ItemSectionInfo itemInfo, WorkflowStatus status)
	{
		return getItemInfo(info).getPrivileges().contains(ExportContentSection.EXPORT_ITEM);
	}

	@Override
	protected SectionId getPageSection()
	{
		return exportContentSection;
	}

	@Override
	public String getLinkText()
	{
		return LINK_LABEL.getText();
	}
}
