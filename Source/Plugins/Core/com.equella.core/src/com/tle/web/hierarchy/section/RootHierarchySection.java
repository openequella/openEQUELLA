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

package com.tle.web.hierarchy.section;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.dytech.edge.web.WebConstants;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.core.institution.InstitutionService;
import com.tle.core.security.TLEAclManager;
import com.tle.exceptions.AccessDeniedException;
import com.tle.web.login.LogonSection;
import com.tle.web.search.base.ContextableSearchSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugURL;
import com.tle.web.sections.equella.layout.ContentLayout;
import com.tle.web.sections.events.BookmarkEvent;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.render.CssInclude;
import com.tle.web.sections.render.Label;
import com.tle.web.template.Breadcrumbs;
import com.tle.web.template.Decorations;
import com.tle.web.template.section.event.BlueBarEvent;
import com.tle.web.template.section.event.BlueBarEventListener;

@SuppressWarnings("nls")
public class RootHierarchySection extends ContextableSearchSection<ContextableSearchSection.Model>
	implements
		BlueBarEventListener
{
	public static final String HIERARCHYURL = "/hierarchy.do";

	@PlugURL("css/hierarchy.css")
	private static String cssUrl;

	@TreeLookup
	private TopicDisplaySection topicSection;

	@Inject
	private TLEAclManager aclManager;
	@Inject
	private InstitutionService institutionService;

	@Override
	protected String getSessionKey()
	{
		return "hierarchyContext";
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		if( aclManager.filterNonGrantedPrivileges(WebConstants.HIERARCHY_PAGE_PRIVILEGE).isEmpty() )
		{
			if( CurrentUser.isGuest() )
			{
				LogonSection.forwardToLogon(context,
					institutionService.removeInstitution(context.getPublicBookmark().getHref()),
					LogonSection.STANDARD_LOGON_PATH);
				return null;
			}
			throw new AccessDeniedException(
				CurrentLocale.get("com.tle.web.hierarchy.missingprivileges", WebConstants.HIERARCHY_PAGE_PRIVILEGE));
		}
		return super.renderHtml(context);
	}

	@Override
	protected void addBreadcrumbsAndTitle(SectionInfo info, Decorations decorations, Breadcrumbs crumbs)
	{
		topicSection.addCrumbs(info, crumbs);
		decorations.setTitle(getTitle(info));
		decorations.setContentBodyClass("browse-layout search-layout");
	}

	@Override
	protected void createCssIncludes(List<CssInclude> includes)
	{
		includes.add(CssInclude.include(cssUrl).hasRtl().make());
		super.createCssIncludes(includes);
	}

	@Override
	public Label getTitle(SectionInfo info)
	{
		return topicSection.getPageTitle(info);
	}

	@Override
	public void addBlueBarResults(RenderContext context, BlueBarEvent event)
	{
		event.addHelp(viewFactory.createResult("hierarchyhelp.ftl", this));
	}

	/**
	 * Browsers show annoying capacity to drop booleans from their event
	 * parameters when that boolean is false. Accordingly the attempt in
	 * ContextableSearchSection.afterParameters to remove previously held values
	 * fails resulting in the 'reverse order' "rs" boolean checkbox becoming
	 * stuck on true/checked once set. The best that can be said about this
	 * workaround is that it's harmless where not required.
	 * 
	 * @param context
	 */
	@Override
	protected boolean hasContextBeenSpecified(SectionInfo info)
	{
		return super.hasContextBeenSpecified(info) || getModel(info).isUpdateContext();
	}

	@Override
	protected Map<String, String[]> buildSearchContext(SectionInfo info)
	{
		final BookmarkEvent bookmarkEvent = new BookmarkEvent();
		// If you ignore browser URL you lose the active topic.
		bookmarkEvent.setIgnoredContexts(BookmarkEvent.CONTEXT_SESSION);
		info.processEvent(bookmarkEvent);
		return bookmarkEvent.getBookmarkState();
	}

	@Override
	protected ContentLayout getDefaultLayout(SectionInfo info)
	{
		return selectionService.getCurrentSession(info) != null ? super.getDefaultLayout(info)
			: ContentLayout.ONE_COLUMN;
	}
}
