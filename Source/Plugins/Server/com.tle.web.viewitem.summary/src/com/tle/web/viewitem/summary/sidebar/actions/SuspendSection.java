package com.tle.web.viewitem.summary.sidebar.actions;

import javax.inject.Inject;

import com.tle.beans.item.ItemStatus;
import com.tle.beans.workflow.WorkflowStatus;
import com.tle.core.guice.Bind;
import com.tle.core.workflow.operations.WorkflowFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.render.Label;
import com.tle.web.viewurl.ItemSectionInfo;

@Bind
public class SuspendSection extends GenericMinorActionSection
{
	@PlugKey("summary.sidebar.actions.suspend.title")
	private static Label LINK_LABEL;
	@PlugKey("summary.sidebar.actions.suspend.confirm")
	private static Label CONFIRM_LABEL;
	@PlugKey("summary.sidebar.actions.suspend.receipt")
	private static Label RECEIPT_LABEL;
	@Inject
	private WorkflowFactory workflowFactory;

	@Override
	@SuppressWarnings("nls")
	protected boolean canView(SectionInfo info, ItemSectionInfo itemInfo, WorkflowStatus status)
	{
		return !status.isLocked() && itemInfo.hasPrivilege("SUSPEND_ITEM")
			&& !status.getStatusName().equals(ItemStatus.SUSPENDED);
	}

	@Override
	protected Label getLinkLabel()
	{
		return LINK_LABEL;
	}

	@Override
	protected Label getConfirmation()
	{
		return CONFIRM_LABEL;
	}

	@Override
	public void execute(SectionInfo info)
	{
		getItemInfo(info).modify(workflowFactory.suspend());
		setReceipt(RECEIPT_LABEL);
	}

	@Override
	public String getLinkText()
	{
		return LINK_LABEL.getText();
	}
}
