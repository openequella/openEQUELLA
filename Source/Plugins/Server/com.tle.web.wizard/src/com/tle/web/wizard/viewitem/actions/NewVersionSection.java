package com.tle.web.wizard.viewitem.actions;

import com.tle.beans.workflow.WorkflowStatus;
import com.tle.core.guice.Bind;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.render.Label;
import com.tle.web.viewurl.ItemSectionInfo;

@Bind
@SuppressWarnings("nls")
public class NewVersionSection extends AbstractWizardViewItemActionSection
{
	@PlugKey("viewitem.actions.newversion.title")
	private static Label LABEL;

	@Override
	protected Label getLinkLabel()
	{
		return LABEL;
	}

	@Override
	protected boolean canView(SectionInfo info, ItemSectionInfo itemInfo, WorkflowStatus status)
	{
		return !status.isLocked() && itemInfo.hasPrivilege("NEWVERSION_ITEM") && itemInfo.hasPrivilege("CREATE_ITEM");
	}

	@Override
	protected void execute(SectionInfo info) throws Exception
	{
		forwardToWizard(info, false, false, true);
	}

	@Override
	public String getLinkText()
	{
		return LABEL.getText();
	}
}
