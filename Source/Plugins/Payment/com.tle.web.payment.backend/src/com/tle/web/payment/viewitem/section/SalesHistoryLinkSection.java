package com.tle.web.payment.viewitem.section;

import com.tle.beans.workflow.WorkflowStatus;
import com.tle.core.guice.Bind;
import com.tle.core.payment.PaymentConstants;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.render.Label;
import com.tle.web.viewitem.summary.sidebar.actions.GenericMinorActionWithPageSection;
import com.tle.web.viewurl.ItemSectionInfo;

@Bind
public class SalesHistoryLinkSection extends GenericMinorActionWithPageSection
{
	@PlugKey("viewitem.summary.sidebar.saleshistory")
	private static Label LINK_LABEL;

	@TreeLookup
	private SalesHistoryContentSection salesHistoryContentSection;

	@Override
	protected SectionId getPageSection()
	{
		return salesHistoryContentSection;
	}

	@Override
	protected Label getLinkLabel()
	{
		return LINK_LABEL;
	}

	@Override
	protected boolean canView(SectionInfo info, ItemSectionInfo itemInfo, WorkflowStatus status)
	{
		return getItemInfo(info).hasPrivilege(PaymentConstants.PRIV_VIEW_SALES_FOR_ITEM);
	}

	@Override
	public String getLinkText()
	{
		return LINK_LABEL.getText();
	}

}
