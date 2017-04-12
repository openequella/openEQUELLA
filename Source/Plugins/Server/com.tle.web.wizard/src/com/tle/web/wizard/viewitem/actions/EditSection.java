package com.tle.web.wizard.viewitem.actions;

import com.tle.beans.workflow.WorkflowStatus;
import com.tle.core.guice.Bind;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.render.Label;
import com.tle.web.viewurl.ItemSectionInfo;

@Bind
@SuppressWarnings("nls")
public class EditSection extends AbstractWizardViewItemActionSection
{
	@PlugKey("viewitem.actions.edit.title")
	private static Label LABEL;

	@Override
	protected Label getLinkLabel()
	{
		return LABEL;
	}

	@Override
	protected boolean canView(SectionInfo info, ItemSectionInfo itemInfo, WorkflowStatus status)
	{
		return !status.isLocked() && itemInfo.hasPrivilege("EDIT_ITEM");
	}

	@Override
	protected void execute(SectionInfo info) throws Exception
	{
		forwardToWizard(info, true, false, false);
	}

	@Override
	public String getLinkText()
	{
		return LABEL.getText();
	}
}
