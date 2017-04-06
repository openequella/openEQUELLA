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
public class ResumeSection extends GenericMinorActionSection
{
	@PlugKey("summary.sidebar.actions.resume.title")
	private static Label LINK_LABEL;
	@PlugKey("summary.sidebar.actions.resume.receipt")
	private static Label RECEIPT_LABEL;

	@Inject
	private WorkflowFactory workflowFactory;

	@Override
	protected Label getLinkLabel()
	{
		return LINK_LABEL;
	}

	@Override
	@SuppressWarnings("nls")
	protected boolean canView(SectionInfo info, ItemSectionInfo itemInfo, WorkflowStatus status)
	{
		return itemInfo.hasPrivilege("SUSPEND_ITEM") && status.getStatusName().equals(ItemStatus.SUSPENDED);
	}

	@Override
	protected void execute(SectionInfo info)
	{
		getItemInfo(info).modify(workflowFactory.resume());
		setReceipt(RECEIPT_LABEL);
	}

	@Override
	public String getLinkText()
	{
		return LINK_LABEL.getText();
	}
}
