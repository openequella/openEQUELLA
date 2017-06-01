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

package com.tle.web.search.actions;

import java.util.List;

import com.tle.annotation.NonNullByDefault;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.equella.layout.TwoColumnLayout;
import com.tle.web.sections.equella.search.AbstractSearchActionsSection;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.render.HtmlRenderer;

@NonNullByDefault
@SuppressWarnings("nls")
public class SearchActionsSection
	extends
		AbstractSearchActionsSection<AbstractSearchActionsSection.AbstractSearchActionsModel> implements HtmlRenderer
{
	@EventFactory
	private EventGenerator events;

	@ViewFactory
	private FreemarkerFactory viewFactory;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		tree.setLayout(id, TwoColumnLayout.RIGHT);
	}

	@Override
	public String[] getResetFilterAjaxIds()
	{
		return new String[]{};
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		renderSectionsToModel(context);

		return viewFactory.createResult("searchactions.ftl", this);
	}

	public List<SectionId> getTopSections()
	{
		return topSections;
	}

	@Override
	public Class<AbstractSearchActionsModel> getModelClass()
	{
		return AbstractSearchActionsModel.class;
	}
}
