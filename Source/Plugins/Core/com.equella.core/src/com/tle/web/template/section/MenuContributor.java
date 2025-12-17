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

package com.tle.web.template.section;

import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.standard.model.HtmlLinkState;
import java.util.List;

public interface MenuContributor {
  static final String KEY_MENU_UPDATED = "MenuUpdated";

  List<MenuContribution> getMenuContributions(SectionInfo info);

  void clearCachedData();

  class MenuContribution {
    private final HtmlLinkState link;
    private final String backgroundImagePath;
    private final int groupPriority;
    private final int linkPriority;
    private final String systemIcon;
    private final String route;
    private boolean customImage;

    /**
     * @param route Menu path to use in the new UI. Only used when the New UI is enabled. If
     *     non-null will be the value used to generate the link for the New UI menu, otherwise a
     *     Legacy UI link will be used
     */
    public MenuContribution(
        HtmlLinkState link,
        String backgroundImage,
        int groupPriority,
        int linkPriority,
        String systemIcon,
        String route) {
      this.link = link;
      this.backgroundImagePath = backgroundImage;
      this.groupPriority = groupPriority;
      this.linkPriority = linkPriority;
      this.systemIcon = systemIcon;
      this.route = route;
    }

    public MenuContribution(
        HtmlLinkState link,
        String backgroundImage,
        int groupPriority,
        int linkPriority,
        String systemIcon) {
      this(link, backgroundImage, groupPriority, linkPriority, systemIcon, null);
    }

    public MenuContribution(
        HtmlLinkState link, String backgroundImage, int groupPriority, int linkPriority) {
      this(link, backgroundImage, groupPriority, linkPriority, null, null);
    }

    public String getRoute() {
      return route;
    }

    public HtmlLinkState getLink() {
      return link;
    }

    public String getBackgroundImagePath() {
      return backgroundImagePath;
    }

    public boolean isCustomImage() {
      return customImage;
    }

    public void setCustomImage(boolean customImage) {
      this.customImage = customImage;
    }

    public int getGroupPriority() {
      return groupPriority;
    }

    public int getLinkPriority() {
      return linkPriority;
    }

    public String getSystemIcon() {
      return systemIcon;
    }
  }
}
