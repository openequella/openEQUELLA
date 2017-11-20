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
