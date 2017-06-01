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
import com.tle.web.wizard.WizardState;
import com.tle.web.wizard.impl.WizardCommand;
import com.tle.web.wizard.section.SelectThumbnailSection;
import com.tle.web.wizard.section.WizardSectionInfo;

public class SelectThumbnail extends WizardCommand
{
	@PlugKey("command.selectthumbnail")
	private static String KEY_SELECT_THUMBNAIL;

	static
	{
		PluginResourceHandler.init(SelectThumbnail.class);
	}

	@SuppressWarnings("nls")
	public SelectThumbnail()
	{
		super(KEY_SELECT_THUMBNAIL, "selectThumbnail");
	}

	@Override
	public JSHandler getJavascript(SectionInfo info, WizardSectionInfo winfo, JSCallable submitFunction)
	{
		SelectThumbnailSection section = info.lookupSection(SelectThumbnailSection.class);
		return new OverrideHandler(section.getOpenFunction());
	}

	@Override
	public void execute(SectionInfo info, WizardSectionInfo winfo, String data) throws Exception
	{
		// nothing here
	}

	@Override
	public boolean isEnabled(SectionInfo info, WizardSectionInfo winfo)
	{
		WizardState state = winfo.getWizardState();
		return ((state.isLockedForEditing() || state.isNewItem() || (!state.isLockedForEditing() && state
			.isRedraftAfterSave())));
	}

	@Override
	public boolean addToMoreActionList()
	{
		return true;
	}

}
