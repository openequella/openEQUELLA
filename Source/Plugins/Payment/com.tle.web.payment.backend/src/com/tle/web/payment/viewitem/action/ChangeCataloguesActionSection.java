package com.tle.web.payment.viewitem.action;

import com.tle.beans.workflow.WorkflowStatus;
import com.tle.core.guice.Bind;
import com.tle.web.payment.viewitem.section.AddRemoveFromCatalogueSection;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.render.Label;
import com.tle.web.viewitem.summary.sidebar.actions.GenericMinorActionWithPageSection;
import com.tle.web.viewurl.ItemSectionInfo;

@Bind
public class ChangeCataloguesActionSection extends GenericMinorActionWithPageSection
{
	@TreeLookup
	private AddRemoveFromCatalogueSection addRemoveFromCatalogueSection;

	@PlugKey("viewitem.addremove.title")
	private static Label LABEL_ACTION;

	@Override
	protected SectionId getPageSection()
	{
		return addRemoveFromCatalogueSection;
	}

	@Override
	protected Label getLinkLabel()
	{
		return LABEL_ACTION;
	}

	@Override
	protected boolean canView(SectionInfo info, ItemSectionInfo itemInfo, WorkflowStatus status)
	{
		return addRemoveFromCatalogueSection.canView(info);
	}

	@Override
	public String getLinkText()
	{
		return LABEL_ACTION.getText();
	}

}
