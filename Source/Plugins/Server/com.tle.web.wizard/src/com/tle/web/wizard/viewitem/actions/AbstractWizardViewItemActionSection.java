package com.tle.web.wizard.viewitem.actions;

import javax.inject.Inject;

import com.tle.beans.item.Item;
import com.tle.web.sections.SectionInfo;
import com.tle.web.viewitem.summary.sidebar.actions.GenericMinorActionSection;
import com.tle.web.wizard.WebWizardService;

public abstract class AbstractWizardViewItemActionSection extends GenericMinorActionSection
{
	@Inject
	private WebWizardService webWizardService;

	protected void forwardToWizard(SectionInfo info, boolean edit, boolean redraft, boolean newVersion)
		throws Exception
	{
		final Item item = getItemInfo(info).getItem();
		webWizardService.forwardToLoadItemWizard(info, item.getUuid(), item.getVersion(), edit, redraft, newVersion);
	}
}
