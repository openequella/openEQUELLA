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

package com.tle.web.searching.section;

import javax.inject.Inject;

import com.dytech.edge.web.WebConstants;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.core.institution.InstitutionService;
import com.tle.core.security.TLEAclManager;
import com.tle.exceptions.AccessDeniedException;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.login.LogonSection;
import com.tle.web.search.base.ContextableSearchSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.layout.ContentLayout;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.generic.InfoBookmark;
import com.tle.web.sections.render.Label;
import com.tle.web.template.section.event.BlueBarEvent;
import com.tle.web.template.section.event.BlueBarEventListener;

@SuppressWarnings("nls")
public class RootSearchSection extends ContextableSearchSection<ContextableSearchSection.Model>
	implements
		BlueBarEventListener
{
	public static final String SEARCHURL = "/searching.do";
	public static final String SEARCH_SESSIONKEY = "searchContext";

	@PlugKey("searching.search.title")
	private static Label LABEL_TITLE;

	@ViewFactory
	private FreemarkerFactory view;

	@Inject
	private TLEAclManager aclManager;
	@Inject
	private InstitutionService institutionService;

	@Override
	public Label getTitle(SectionInfo info)
	{
		return LABEL_TITLE;
	}

	@Override
	protected String getSessionKey()
	{
		return SEARCH_SESSIONKEY;
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		if( aclManager.filterNonGrantedPrivileges(WebConstants.SEARCH_PAGE_PRIVILEGE).isEmpty() )
		{
			if( CurrentUser.isGuest() )
			{
				LogonSection.forwardToLogon(context,
					institutionService.removeInstitution(new InfoBookmark(context).getHref()),
					LogonSection.STANDARD_LOGON_PATH);
				return null;
			}
			throw new AccessDeniedException(
				CurrentLocale.get("com.tle.web.searching.missingprivileges", WebConstants.SEARCH_PAGE_PRIVILEGE));
		}
		return super.renderHtml(context);
	}

	public static SectionInfo createForward(SectionInfo from)
	{
		return from.createForward(SEARCHURL);
	}

	@Override
	public void addBlueBarResults(RenderContext context, BlueBarEvent event)
	{
		event.addHelp(view.createResult("searching-help.ftl", this));
	}

	@Override
	protected ContentLayout getDefaultLayout(SectionInfo info)
	{
		return selectionService.getCurrentSession(info) != null ? super.getDefaultLayout(info)
			: ContentLayout.ONE_COLUMN;
	}
}
