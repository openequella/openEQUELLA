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

package com.tle.web.portal.section.admin;

import javax.inject.Inject;

import com.tle.web.portal.section.common.SelectNewPortletTypeSection;
import com.tle.web.portal.service.PortletWebService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.template.section.HelpAndScreenOptionsSection;

public class AdminSelectNewPortletTypeSection extends SelectNewPortletTypeSection
{
	@Inject
	private PortletWebService portletWebService;

	@Override
	@EventHandlerMethod
	public void typeSelected(SectionInfo info, String type)
	{
		portletWebService.newPortlet(info, type, true);
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		getModel(context).setNoCreatePrivs(!portletWebService.canCreate());
		HelpAndScreenOptionsSection.addScreenOptions(context, renderOptions(context));
		return null;
	}

	@Override
	protected boolean isAdmin()
	{
		return true;
	}
}
