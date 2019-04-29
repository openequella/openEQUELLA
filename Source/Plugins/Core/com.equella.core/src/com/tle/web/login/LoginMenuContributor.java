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

package com.tle.web.login;

import com.tle.core.guice.Bind;
import com.tle.core.institution.InstitutionService;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.template.section.MenuContributor;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;

@Bind
@Singleton
@SuppressWarnings("nls")
public class LoginMenuContributor implements MenuContributor {
  private static final Label LABEL =
      new KeyLabel(
          ResourcesService.getResourceHelper(LoginMenuContributor.class).key("menu.login"));
  private static final String ICON_URL =
      ResourcesService.getResourceHelper(LoginMenuContributor.class)
          .url("images/menu-icon-login.png");

  @Inject private InstitutionService institutionService;

  @Override
  public List<MenuContribution> getMenuContributions(SectionInfo info) {
    String relUrl = institutionService.removeInstitution(info.getPublicBookmark().getHref());
    HtmlLinkState hls =
        new HtmlLinkState(
            LogonSection.forwardToLogonBookmark(
                info,
                relUrl,
                institutionService.institutionalise(LogonSection.STANDARD_LOGON_PATH)));
    hls.setLabel(LABEL);
    MenuContribution mc = new MenuContribution(hls, ICON_URL, 1, 1);
    return Collections.singletonList(mc);
  }

  @Override
  public void clearCachedData() {
    // Nothing is cached
  }
}
