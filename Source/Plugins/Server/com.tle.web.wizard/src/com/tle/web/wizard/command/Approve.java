/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
			return !tls.isEditing();
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
