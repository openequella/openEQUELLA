package com.tle.web.payment.viewitem.action;

import com.tle.beans.workflow.WorkflowStatus;
import com.tle.core.guice.Bind;
import com.tle.web.payment.viewitem.section.ChangePricingTierContentSection;
import com.tle.web.payment.viewitem.section.ChangePricingTierSection;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.render.Label;
import com.tle.web.viewitem.summary.sidebar.actions.GenericMinorActionWithPageSection;
import com.tle.web.viewurl.ItemSectionInfo;

@Bind
public class ChangePricingTierActionSection extends GenericMinorActionWithPageSection
{
	@TreeLookup
	private ChangePricingTierContentSection changePricingTierContentSection;

	@PlugKey("viewitem.tier.action")
	private static Label LINK_LABEL;

	@Override
	protected Label getLinkLabel()
	{
		return LINK_LABEL;
	}

	@Override
	protected boolean canView(SectionInfo info, ItemSectionInfo itemInfo, WorkflowStatus status)
	{
		return ChangePricingTierSection.canView(info);
	}

	@Override
	protected SectionId getPageSection()
	{
		return changePricingTierContentSection;
	}

	@Override
	public String getLinkText()
	{
		return LINK_LABEL.getText();
	}

}