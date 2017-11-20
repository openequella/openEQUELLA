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

package com.tle.web.itemadmin.section;

import javax.inject.Inject;

import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.itemadmin.ItemAdminPrivilegeTreeProvider;
import com.tle.web.search.base.ContextableSearchSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.render.Label;
import com.tle.web.template.section.event.BlueBarEvent;
import com.tle.web.template.section.event.BlueBarEventListener;

@SuppressWarnings("nls")
public class RootItemAdminSection extends ContextableSearchSection<ContextableSearchSection.Model>
	implements
		BlueBarEventListener
{
	public static final String ITEMADMINURL = "/access/itemadmin.do";

	@Inject
	private ItemAdminPrivilegeTreeProvider securityProvider;

	@PlugKey("itemadmin.title")
	private static Label title;

	@ViewFactory
	private FreemarkerFactory view;

	@Override
	protected String getSessionKey()
	{
		return "itemadminContext";
	}

	@Override
	public Label getTitle(SectionInfo info)
	{
		return title;
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		securityProvider.checkAuthorised();
		return super.renderHtml(context);
	}

	@Override
	public void addBlueBarResults(RenderContext context, BlueBarEvent event)
	{
		event.addHelp(view.createResult("itemadmin-help.ftl", this));
	}
}
