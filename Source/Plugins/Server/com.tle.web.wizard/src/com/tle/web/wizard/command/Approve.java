package com.tle.web.wizard.command;

import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.annotation.PluginResourceHandler;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.wizard.impl.WizardCommand;
import com.tle.web.wizard.section.WizardSectionInfo;
import com.tle.web.workflow.tasks.dialog.ApproveDialog;
import com.tle.web.workflow.tasks.CurrentTaskSection;
import com.tle.web.workflow.tasks.TaskListState;
import com.tle.web.workflow.tasks.comments.CommentsSection;

public class Approve extends WizardCommand
{
	static
	{
		PluginResourceHandler.init(Approve.class);
	}

	@PlugKey("command.approve.name")
	private static String KEY_NAME;

	public Approve()
	{
		super(KEY_NAME, "approve"); //$NON-NLS-1$
	}

	@Override
	public JSHandler getJavascript(SectionInfo info, WizardSectionInfo winfo, JSCallable submitFunc)
	{
		ApproveDialog approveDialog = info.lookupSection(ApproveDialog.class);
		return new OverrideHandler(approveDialog.getOpenFunction());
	}

	@Override
	public boolean isEnabled(SectionInfo info, WizardSectionInfo winfo)
	{
		CurrentTaskSection currentTaskSection = info.lookupSection(CurrentTaskSection.class);
		TaskListState tls = (currentTaskSection == null ? null : currentTaskSection.getCurrentState(info));
		if (tls != null)
		{
			CommentsSection commentsSection = info.lookupSection(CommentsSection.class);
			return !tls.isEditing() && (commentsSection == null || !commentsSection.isCommenting(info));
		}
		return false;
	}

	@Override
	public void execute(SectionInfo info, WizardSectionInfo winfo, String data) throws Exception
	{
	}

	@Override
	public boolean isMajorAction()
	{
		return true;
	}

	@Override
	public String getStyleClass()
	{
		return "moderate-approve";
	}
}
