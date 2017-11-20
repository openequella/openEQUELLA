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

package com.tle.web.wizard.standard.controls;

import com.tle.core.guice.Bind;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.js.ElementId;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.wizard.controls.AbstractSimpleWebControl;
import com.tle.web.wizard.controls.CButton;

/**
 * @author jmaginnis
 */
@Bind
public class Button extends AbstractSimpleWebControl
{
	@Component(stateful = false)
	private com.tle.web.sections.standard.Button button;
	@EventFactory
	private EventGenerator events;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		button.setClickHandler(events.getNamedHandler("clicked")); //$NON-NLS-1$
	}

	@EventHandlerMethod
	public void clicked(SectionInfo info)
	{
		((CButton) getWrappedControl()).setActionFired(true);
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		addDisabler(context, button);
		button.setLabel(context, new TextLabel(getTitle()));
		return SectionUtils.renderSection(context, button.getSectionId());
	}

	@Override
	public void doEdits(SectionInfo info)
	{
		// nothing
	}

	@Override
	protected ElementId getIdForLabel()
	{
		return button;
	}
}
