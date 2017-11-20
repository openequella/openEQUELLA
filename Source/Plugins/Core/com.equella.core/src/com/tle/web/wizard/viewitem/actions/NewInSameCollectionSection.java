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
