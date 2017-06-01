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

import com.tle.common.i18n.CurrentLocale;
import com.tle.exceptions.AccessDeniedException;
import com.tle.web.portal.section.enduser.ModalPortletSection;
import com.tle.web.portal.service.PortletWebService;
import com.tle.web.search.base.ContextableSearchSection;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.annotations.DirectEvent;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.layout.ContentLayout;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.render.Label;
import com.tle.web.settings.menu.SettingsUtils;
import com.tle.web.template.Breadcrumbs;
import com.tle.web.template.Decorations;
import com.tle.web.template.section.event.BlueBarEvent;
import com.tle.web.template.section.event.BlueBarEventListener;

/**
 * @author aholland
 */
@SuppressWarnings("nls")
public class RootPortletAdminSection extends ContextableSearchSection<ContextableSearchSection.Model>
	implements
		BlueBarEventListener
{
	private static final String CONTEXT_KEY = "portletAdminContext";

	@PlugKey("page.admin.title")
	private static Label TITLE_LABEL;
	@PlugKey("admin.error.accessdenied")
	private static String ERROR_KEY;

	@Inject
	private PortletWebService portletWebService;

	@DirectEvent
	public void ensurePrivs(SectionInfo info)
	{
		if( !portletWebService.canAdminister() )
		{
			throw new AccessDeniedException(CurrentLocale.get(ERROR_KEY));
		}
	}

	@Override
	protected String getContentBodyClasses()
	{
		return super.getContentBodyClasses() + " portaladmin";
	}

	@Override
	protected void addBreadcrumbsAndTitle(SectionInfo info, Decorations decorations, Breadcrumbs crumbs)
	{
		TwoColumnModel model = getModel(info);
		SectionId modalSection = model.getModalSection();
		if( modalSection != null )
		{
			SectionId section = info.getSectionForId(modalSection);
			if( section instanceof ModalPortletSection )
			{
				((ModalPortletSection) section).addBreadcrumbsAndTitle(info, decorations, crumbs);
				return;
			}
		}
		super.addBreadcrumbsAndTitle(info, decorations, crumbs);
		Breadcrumbs.get(info).add(SettingsUtils.getBreadcrumb());
	}

	@Override
	public Label getTitle(SectionInfo info)
	{
		return TITLE_LABEL;
	}

	@Override
	protected String getSessionKey()
	{
		return CONTEXT_KEY;
	}

	@Override
	public void addBlueBarResults(RenderContext context, BlueBarEvent event)
	{
		event.addHelp(viewFactory.createResult("admin/help.ftl", this));
	}

	@Override
	protected ContentLayout getDefaultLayout(SectionInfo info)
	{
		return ContentLayout.ONE_COLUMN;
	}
}
