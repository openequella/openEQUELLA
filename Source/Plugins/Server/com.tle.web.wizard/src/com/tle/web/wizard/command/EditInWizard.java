package com.tle.web.wizard.command;

import javax.inject.Inject;

import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.annotation.PluginResourceHandler;
import com.tle.web.wizard.WizardService;
import com.tle.web.wizard.impl.WizardCommand;
import com.tle.web.wizard.section.WizardSectionInfo;
import com.tle.web.workflow.tasks.ModerationService;

@SuppressWarnings("nls")
public class EditInWizard extends WizardCommand
{
	static
	{
		PluginResourceHandler.init(EditInWizard.class);
	}

	@PlugKey("command.edit.name")
	private static String KEY_NAME;

	@Inject
	private ModerationService moderationService;
	@Inject
	private WizardService wizardService;

	public EditInWizard()
	{
		super(KEY_NAME, "edit"); //$NON-NLS-1$
	}

	@Override
	public boolean isEnabled(SectionInfo info, WizardSectionInfo winfo)
	{
		return !(winfo.isLockedForEditing() || winfo.isNewItem()) && winfo.isAvailableForEditing()
			&& winfo.hasPrivilege("EDIT_ITEM");
	}

	@Override
	public void execute(SectionInfo info, WizardSectionInfo winfo, String data) throws Exception
	{
		wizardService.reload(winfo.getWizardState(), true);
		moderationService.setEditing(info, true);
	}

	@Override
	public boolean isMajorAction()
	{
		return true;
	}

	@Override
	public String getStyleClass()
	{
		return "edit";
	}
}
