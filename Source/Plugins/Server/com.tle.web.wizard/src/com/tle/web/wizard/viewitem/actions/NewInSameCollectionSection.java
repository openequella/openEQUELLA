package com.tle.web.wizard.viewitem.actions;

import javax.inject.Inject;

import com.tle.beans.item.Item;
import com.tle.beans.workflow.WorkflowStatus;
import com.tle.core.guice.Bind;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.render.Label;
import com.tle.web.viewurl.ItemSectionInfo;
import com.tle.web.wizard.WebWizardService;

@Bind
@SuppressWarnings("nls")
public class NewInSameCollectionSection extends AbstractWizardViewItemActionSection
{
	@PlugKey("viewitem.actions.newinsamecollection.title")
	private static Label LABEL;

	@Inject
	private WebWizardService webWizardService;

	@Override
	protected Label getLinkLabel()
	{
		return LABEL;
	}

	@Override
	protected boolean canView(SectionInfo info, ItemSectionInfo itemInfo, WorkflowStatus status)
	{
		return itemInfo.hasPrivilege("CREATE_ITEM");
	}

	@Override
	protected void execute(SectionInfo info) throws Exception
	{
		final Item item = getItemInfo(info).getItem();
		webWizardService.forwardToNewItemWizard(info, item.getItemDefinition().getUuid(), null, null, true);
	}

	@Override
	public String getLinkText()
	{
		return LABEL.getText();
	}
}
