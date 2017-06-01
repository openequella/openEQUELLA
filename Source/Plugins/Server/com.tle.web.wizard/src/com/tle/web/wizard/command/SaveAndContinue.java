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
import com.tle.web.sections.equella.receipt.ReceiptService;
import com.tle.web.sections.render.Label;
import com.tle.web.wizard.WizardService;
import com.tle.web.wizard.WizardState;
import com.tle.web.wizard.impl.WizardCommand;
import com.tle.web.wizard.section.WizardSectionInfo;
import com.tle.web.workflow.tasks.ModerationService;

public class SaveAndContinue extends WizardCommand
{
	@PlugKey("command.saveandcontinue")
	private static String KEY_SAVE_AND_CONTINUE;
	@PlugKey("command.save.successandcontinuereceipt")
	private static Label SUCCESS_CONTINUE_RECEIPT_LABEL;

	@Inject
	private WizardService wizardService;
	@Inject
	private ModerationService moderationService;
	@Inject
	private ReceiptService receiptService;

	static
	{
		PluginResourceHandler.init(SaveAndContinue.class);
	}

	@SuppressWarnings("nls")
	public SaveAndContinue()
	{
		super(KEY_SAVE_AND_CONTINUE, "saveAndContinue");
	}

	@Override
	public void execute(SectionInfo info, WizardSectionInfo winfo, String data) throws Exception
	{
		WizardState state = winfo.getWizardState();
		wizardService.doSave(state, false);
		receiptService.setReceipt(SUCCESS_CONTINUE_RECEIPT_LABEL);
		wizardService.reloadSaveAndContinue(state);
		moderationService.setEditing(info, true);
	}

	@Override
	public boolean isEnabled(SectionInfo info, WizardSectionInfo winfo)
	{
		WizardState state = winfo.getWizardState();
		return (state.isLockedForEditing() || state.isNewItem() || (!state.isLockedForEditing() && state
			.isRedraftAfterSave()));
	}

}
