package com.tle.web.viewitem.summary.sidebar.actions;

import com.tle.beans.workflow.WorkflowStatus;
import com.tle.core.guice.Bind;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.render.Label;
import com.tle.web.viewitem.summary.content.ChangeOwnershipContentSection;
import com.tle.web.viewurl.ItemSectionInfo;

/**
 * @author Andrew Gibb
 */
@Bind
public class ChangeOwnershipSection extends GenericMinorActionWithPageSection
{
	@PlugKey("summary.sidebar.actions.changeownership.title")
	private static Label LINK_LABEL;

	@TreeLookup
	private ChangeOwnershipContentSection changeOwnershipContentSection;

	@Override
	protected Label getLinkLabel()
	{
		return LINK_LABEL;
	}

	@Override
	protected boolean canView(SectionInfo info, ItemSectionInfo itemInfo, WorkflowStatus status)
	{
		return !status.isLocked() && itemInfo.hasPrivilege(ChangeOwnershipContentSection.REQUIRED_PRIVILEGE);
	}

	@Override
	protected SectionId getPageSection()
	{
		return changeOwnershipContentSection;
	}

	@Override
	public String getLinkText()
	{
		return LINK_LABEL.getText();
	}
}
