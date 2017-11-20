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

package com.tle.web.activation.section;

import javax.inject.Inject;

import com.tle.web.activation.ActivationsPrivilegeTreeProvider;
import com.tle.web.search.base.ContextableSearchSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.layout.ContentLayout;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.render.Label;

public class RootActivationSection extends ContextableSearchSection<ContextableSearchSection.Model>
{
	public static final String ACTIVATIONURL = "/access/activations.do"; //$NON-NLS-1$

	@Inject
	private ActivationsPrivilegeTreeProvider securityProvider;

	@PlugKey("manageactivations.title")
	private static Label title;

	@Override
	protected String getSessionKey()
	{
		return "activationContext"; //$NON-NLS-1$
	}

	@Override
	public Label getTitle(SectionInfo info)
	{
		return title;
	}

	@SuppressWarnings("nls")
	@Override
	protected String getContentBodyClasses()
	{
		return super.getContentBodyClasses() + " activations-layout";
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		securityProvider.checkAuthorised();
		return super.renderHtml(context);
	}

	@Override
	protected ContentLayout getDefaultLayout(SectionInfo info)
	{
		return ContentLayout.TWO_COLUMN;
	}
}
