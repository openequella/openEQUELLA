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

package com.tle.web.selection.home.sections;

import javax.inject.Inject;

import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.equella.layout.TwoColumnLayout;
import com.tle.web.sections.equella.layout.TwoColumnLayout.TwoColumnModel;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.selection.SelectionService;
import com.tle.web.selection.SelectionSession;
import com.tle.web.template.Breadcrumbs;
import com.tle.web.template.Decorations;

@SuppressWarnings("nls")
public class RootSelectionHomeSection extends TwoColumnLayout<TwoColumnModel>
{
	private static final PluginResourceHelper RESOURCES = ResourcesService
		.getResourceHelper(RootSelectionHomeSection.class);

	@Inject
	private SelectionService selectionService;

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		SelectionSession session = selectionService.getCurrentSession(context);
		if( session == null )
		{
			throw new RuntimeException(RESOURCES.getString("error.requiresselectionsession"));
		}

		return super.renderHtml(context);
	}

	@Override
	protected void addBreadcrumbsAndTitle(SectionInfo info, Decorations decorations, Breadcrumbs crumbs)
	{
		decorations.setContentBodyClass("selectiondashboard");
	}

	@Override
	public Class<TwoColumnModel> getModelClass()
	{
		return TwoColumnModel.class;
	}
}
