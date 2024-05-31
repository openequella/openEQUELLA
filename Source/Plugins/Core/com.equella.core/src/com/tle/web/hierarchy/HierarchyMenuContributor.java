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

package com.tle.web.hierarchy;

import static com.tle.common.hierarchy.VirtualTopicUtils.buildTopicId;

import com.dytech.edge.web.WebConstants;
import com.google.common.collect.Lists;
import com.tle.beans.hierarchy.HierarchyTopic;
import com.tle.common.NameValue;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.guice.Bind;
import com.tle.core.hierarchy.HierarchyService;
import com.tle.core.search.VirtualisableAndValue;
import com.tle.core.security.TLEAclManager;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.model.SimpleBookmark;
import com.tle.web.template.NewUiRoutes;
import com.tle.web.template.RenderNewTemplate;
import com.tle.web.template.section.AbstractUpdatableMenuContributor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;

@Bind
@Singleton
@SuppressWarnings("nls")
public class HierarchyMenuContributor extends AbstractUpdatableMenuContributor {
  private static final String ICON_PATH =
      ResourcesService.getResourceHelper(HierarchyMenuContributor.class)
          .url("images/menu-icon-hierarchy.png");
  private static final String SESSION_KEY = "HIERARCHY-MENU";
  private static final String SHOW_MORE = "SHOW-MORE";

  @PlugKey("label.more")
  private static Label MORE;

  @Inject private TLEAclManager aclManager;
  @Inject private HierarchyService hierarchyService;

  @Override
  public List<MenuContribution> getMenuContributions(SectionInfo info) {
    if (aclManager.filterNonGrantedPrivileges(WebConstants.HIERARCHY_PAGE_PRIVILEGE).isEmpty()) {
      return Collections.emptyList();
    }

    Boolean showMoreLink = userSessionService.getAttribute(SHOW_MORE);
    List<NameValue> topics = userSessionService.getAttribute(SESSION_KEY);

    if (topics == null) {
      showMoreLink = false;

      List<VirtualisableAndValue<HierarchyTopic>> pageTopics =
          hierarchyService.expandVirtualisedTopics(hierarchyService.getRootTopics(), null, null);
      Iterator<VirtualisableAndValue<HierarchyTopic>> iter = pageTopics.iterator();

      int show = pageTopics.size();
      if (pageTopics.size() > 5) {
        show = 4;
        showMoreLink = true;
      }

      topics = Lists.newArrayListWithExpectedSize(show);
      for (int i = 0; i < show; i++) {
        VirtualisableAndValue<HierarchyTopic> pair = iter.next();
        HierarchyTopic topic = pair.getVt();
        String virtValue = pair.getVirtualisedValue();
        String topicId = buildTopicId(topic, virtValue, null);
        String name = CurrentLocale.get(topic.getName());
        if (virtValue != null) {
          name = name.replaceAll("%s", virtValue);
        }
        topics.add(new NameValue(name, topicId));
      }

      userSessionService.setAttribute(SESSION_KEY, topics);
      userSessionService.setAttribute(SHOW_MORE, showMoreLink);
    }

    if (topics.isEmpty()) {
      return Collections.emptyList();
    }

    int linkPriority = 0;
    List<MenuContribution> mcs = new ArrayList<MenuContribution>();
    boolean newUIEnabled = RenderNewTemplate.isNewUIEnabled();
    for (NameValue topic : topics) {
      String encodedTopicUuid = topic.getValue();

      HtmlLinkState hls =
          new HtmlLinkState(new SimpleBookmark("hierarchy.do?topic=" + encodedTopicUuid));
      String route = newUIEnabled ? "/" + NewUiRoutes.hierarchy(encodedTopicUuid) : null;
      hls.setLabel(new TextLabel(topic.getName()));

      MenuContribution mc =
          new MenuContribution(hls, ICON_PATH, 10, linkPriority++, "device_hub", route);
      mcs.add(mc);
    }

    if (showMoreLink) {
      HtmlLinkState hls = new HtmlLinkState(new SimpleBookmark("hierarchy.do?topic=ALL"));
      hls.setLabel(MORE);

      String browseRoute = newUIEnabled ? "/" + NewUiRoutes.PATH_BROWSE_HIERARCHIES() : null;
      MenuContribution mc =
          new MenuContribution(hls, ICON_PATH, 10, linkPriority++, null, browseRoute);
      mcs.add(mc);
    }

    return mcs;
  }

  @Override
  public String getSessionKey() {
    return SESSION_KEY;
  }
}
