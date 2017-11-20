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
