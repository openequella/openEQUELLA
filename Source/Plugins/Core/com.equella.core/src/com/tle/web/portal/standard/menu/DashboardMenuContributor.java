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

package com.tle.web.portal.standard.menu;

import com.dytech.edge.web.WebConstants;
import com.tle.core.guice.Bind;
import com.tle.core.security.TLEAclManager;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.model.SimpleBookmark;
import com.tle.web.template.NewUiRoutes;
import com.tle.web.template.RenderNewTemplate;
import com.tle.web.template.section.MenuContributor;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;

@Bind
@Singleton
@SuppressWarnings("nls")
public class DashboardMenuContributor implements MenuContributor {
  private static final Label LABEL =
      new KeyLabel(
          ResourcesService.getResourceHelper(DashboardMenuContributor.class).key("menu.dashboard"));
  private static final String ICON_URL =
      ResourcesService.getResourceHelper(DashboardMenuContributor.class)
          .url("images/menu-icon-dashboard.png");

  @Inject private TLEAclManager aclManager;

  @Override
  public List<MenuContribution> getMenuContributions(SectionInfo info) {
    if (aclManager.filterNonGrantedPrivileges(WebConstants.DASHBOARD_PAGE_PRIVILEGE).isEmpty()) {
      return Collections.emptyList();
    }

    boolean newUIEnabled = RenderNewTemplate.isNewUIEnabled();
    String newUiRoute = newUIEnabled ? "/" + NewUiRoutes.PATH_DASHBOARD() : null;
    String dashboardPath = newUIEnabled ? NewUiRoutes.PATH_DASHBOARD() : "home.do";

    HtmlLinkState hls = new HtmlLinkState(new SimpleBookmark(dashboardPath));
    hls.setLabel(LABEL);
    MenuContribution mc = new MenuContribution(hls, ICON_URL, 1, 1, "dashboard", newUiRoute);
    return Collections.singletonList(mc);
  }

  @Override
  public void clearCachedData() {
    // Nothing is cached
  }
}
