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

package com.tle.web.myresources;

import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.search.base.ContextableSearchSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.layout.ContentLayout;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.render.Label;
import com.tle.web.template.section.event.BlueBarEvent;
import com.tle.web.template.section.event.BlueBarEventListener;

public class RootMyResourcesSection extends ContextableSearchSection<ContextableSearchSection.Model>
	implements
		BlueBarEventListener
{
	private static final String CONTEXT_KEY = "myResourcesContext"; //$NON-NLS-1$
	private static final String URL = "/access/myresources.do"; //$NON-NLS-1$

	@ViewFactory
	private FreemarkerFactory view;

	@PlugKey("menu")
	private static Label title;

	@TreeLookup
	private MyResourcesSearchTypeSection searchTypeSection;

	@Override
	protected String getSessionKey()
	{
		return CONTEXT_KEY;
	}

	@Override
	public Label getTitle(SectionInfo info)
	{
		return title;
	}

	public static SectionInfo createForward(SectionInfo from)
	{
		return from.createForward(URL);
	}

	@Override
	protected ContentLayout getDefaultLayout(SectionInfo info)
	{
		return selectionService.getCurrentSession(info) != null ? super.getDefaultLayout(info)
			: ContentLayout.ONE_COLUMN;
	}

	@Override
	public void addBlueBarResults(RenderContext context, BlueBarEvent event)
	{
		event.addHelp(view.createResult("mainhelp.ftl", this)); //$NON-NLS-1$
	}

	@Override
	protected boolean hasContextBeenSpecified(SectionInfo info)
	{
		return getModel(info).isUpdateContext();
	}
}
