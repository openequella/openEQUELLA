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

package com.tle.web.contribute;

import java.util.List;

import javax.inject.Inject;

import com.tle.beans.item.Item;
import com.tle.core.guice.Bind;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.events.js.SubmitValuesFunction;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.validators.Confirm;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.viewitem.section.ParentViewItemSectionUtils;
import com.tle.web.viewurl.ItemSectionInfo;
import com.tle.web.wizard.WebWizardService;
import com.tle.web.wizard.WizardInfo;
import com.tle.web.wizard.WizardService;

@Bind
@SuppressWarnings("nls")
public class ResumeSection extends AbstractPrototypeSection<Object> implements HtmlRenderer
{
	@PlugKey("resume.confirm")
	private static Confirm RESUME_CONFIRM;

	@EventFactory
	private EventGenerator events;

	@Inject
	private WizardService wizardService;

	@Inject
	private WebWizardService webWizardService;

	@Component
	@PlugKey("resume")
	private Button resume;

	private SubmitValuesFunction resumeHandler;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		resumeHandler = events.getSubmitValuesFunction("resume");
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		final ItemSectionInfo itemInfo = ParentViewItemSectionUtils.getItemInfo(context);
		Item item = itemInfo.getItem();
		List<WizardInfo> resumableWizards = wizardService.listWizardsInSession();
		if( resumableWizards.size() > 0 )
		{
			for( WizardInfo wizInfo : resumableWizards )
			{
				if( wizInfo.getItemUuid().equals(item.getUuid()) && wizInfo.getItemVersion() == item.getVersion() )
				{
					resume.setClickHandler(context,
						new OverrideHandler(resumeHandler, wizInfo.getUuid()).addValidator(RESUME_CONFIRM));
					return renderSection(context, resume);
				}
			}
		}
		return null;
	}

	@EventHandlerMethod
	public void resume(SectionInfo info, String wizardUuid)
	{
		webWizardService.forwardToLoadWizard(info, wizardUuid);
	}
}
