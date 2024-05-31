/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
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

import com.dytech.edge.web.WebConstants;
import com.tle.common.Check;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.core.institution.InstitutionService;
import com.tle.core.security.TLEAclManager;
import com.tle.exceptions.AccessDeniedException;
import com.tle.web.login.LogonSection;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
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
import com.tle.web.sections.render.SimpleSectionResult;
import com.tle.web.template.Breadcrumbs;
import com.tle.web.template.Decorations;
import com.tle.web.template.RenderNewSearchPage;
import com.tle.web.template.section.event.BlueBarEvent;
import com.tle.web.template.section.event.BlueBarEventListener;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;

@SuppressWarnings("nls")
public class RootHierarchySection extends ContextableSearchSection<ContextableSearchSection.Model>
    implements BlueBarEventListener {
  private static PluginResourceHelper urlHelper =
      ResourcesService.getResourceHelper(RootHierarchySection.class);
  public static final String HIERARCHYURL = "/hierarchy.do";

  @PlugURL("css/hierarchy.css")
  private static String cssUrl;

  @TreeLookup private TopicDisplaySection topicSection;

  @Inject private TLEAclManager aclManager;
  @Inject private InstitutionService institutionService;

  @Override
  protected String getSessionKey() {
    return "hierarchyContext";
  }

  @Override
  public SectionResult renderHtml(RenderEventContext context) {
    if (aclManager.filterNonGrantedPrivileges(WebConstants.HIERARCHY_PAGE_PRIVILEGE).isEmpty()) {
      if (CurrentUser.isGuest()) {
        LogonSection.forwardToLogon(
            context,
            institutionService.removeInstitution(context.getPublicBookmark().getHref()),
            LogonSection.STANDARD_LOGON_PATH);
        return null;
      }
      throw new AccessDeniedException(
          urlHelper.getString("missingprivileges", WebConstants.HIERARCHY_PAGE_PRIVILEGE));
    }

    if (isNewUIInSelectionSession(context)) {
      SimpleSectionResult newUIContent =
          isBrowseHierarchy(context)
              ? RenderNewSearchPage.renderNewHierarchyBrowsePage(context)
              : RenderNewSearchPage.renderNewHierarchyPage(context);

      getModel(context).setNewUIContent(newUIContent);
    }
    return super.renderHtml(context);
  }

  @Override
  protected void addBreadcrumbsAndTitle(
      SectionInfo info, Decorations decorations, Breadcrumbs crumbs) {
    // Skip Breadcrumbs when the New UI is used in Selection Session
    if (!isNewUIInSelectionSession(info)) {
      topicSection.addCrumbs(info, crumbs);
    }

    decorations.setTitle(getTitle(info));
    decorations.setContentBodyClass("browse-layout search-layout");
  }

  @Override
  protected void createCssIncludes(List<CssInclude> includes) {
    includes.add(CssInclude.include(cssUrl).hasRtl().make());
    super.createCssIncludes(includes);
  }

  @Override
  public Label getTitle(SectionInfo info) {
    return topicSection.getPageTitle(info);
  }

  @Override
  public void addBlueBarResults(RenderContext context, BlueBarEvent event) {
    event.addHelp(viewFactory.createResult("hierarchyhelp.ftl", this));
  }

  @Override
  protected Map<String, String[]> buildSearchContext(SectionInfo info) {
    final BookmarkEvent bookmarkEvent = new BookmarkEvent();
    // If you ignore browser URL you lose the active topic.
    bookmarkEvent.setIgnoredContexts(BookmarkEvent.CONTEXT_SESSION);
    info.processEvent(bookmarkEvent);
    return bookmarkEvent.getBookmarkState();
  }

  @Override
  protected ContentLayout getDefaultLayout(SectionInfo info) {
    return selectionService.getCurrentSession(info) != null
        ? super.getDefaultLayout(info)
        : ContentLayout.ONE_COLUMN;
  }

  @Override
  protected String getPageName() {
    return HIERARCHYURL;
  }

  private boolean isBrowseHierarchy(SectionInfo info) {
    String topicId = topicSection.getModel(info).getTopicId();
    return Check.isEmpty(topicId) || TopicDisplaySection.ROOT_TOPICS.equals(topicId);
  }
}
