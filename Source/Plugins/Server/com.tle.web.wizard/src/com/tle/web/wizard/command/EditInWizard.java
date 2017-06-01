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
