package com.tle.web.wizard.command;

import javax.inject.Inject;

import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.annotation.PluginResourceHandler;
import com.tle.web.wizard.impl.WizardCommand;
import com.tle.web.wizard.section.WizardSectionInfo;
import com.tle.web.workflow.tasks.ModerationService;

public class ViewSummary extends WizardCommand
{
	static
	{
		PluginResourceHandler.init(ViewSummary.class);
	}

	@PlugKey("command.view.name")
	private static String KEY_NAME;

	@Inject
	private ModerationService moderationService;

	@SuppressWarnings("nls")
	public ViewSummary()
	{
		super(KEY_NAME, "view");
	}

	@Override
	public boolean isMajorAction()
	{
		return true;
	}

	@Override
	public String getStyleClass()
	{
		return "view"; //$NON-NLS-1$
	}

	@Override
	public boolean isEnabled(SectionInfo info, WizardSectionInfo winfo)
	{
		return moderationService.isModerating(info) && !(winfo.isLockedForEditing());
	}

	@Override
	public void execute(SectionInfo info, WizardSectionInfo winfo, String data) throws Exception
	{
		moderationService.viewSummary(info);
	}

}
