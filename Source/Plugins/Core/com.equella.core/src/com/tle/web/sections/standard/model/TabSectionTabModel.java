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

package com.tle.web.sections.standard.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.tle.common.NameValue;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderEvent;
import com.tle.web.sections.render.ResultListCollector;
import com.tle.web.sections.render.SectionRenderable;

public class TabSectionTabModel implements TabModel {
  private Map<String, List<TabSection>> tabSections = new LinkedHashMap<String, List<TabSection>>();

  private Map<String, TabContent> getVisibleTabsCached(SectionInfo info) {
    Map<String, TabContent> tabs = info.getAttribute(this);
    if (tabs == null) {
      tabs = new LinkedHashMap<String, TabContent>();
      for (String tabValue : tabSections.keySet()) {
        List<TabSection> sections = tabSections.get(tabValue);
        for (TabSection tabSection : sections) {
          if (tabSection.isVisible(info)) {
            NameValue tabNv = tabSection.getTabToAppearOn();
            TabContent tabContent =
                new TabContent(
                    tabs.size(), tabNv.getName(), tabNv.getValue(), new TabRenderer(sections));
            tabs.put(tabNv.getValue(), tabContent);
            break;
          }
        }
      }
    }
    return tabs;
  }

  @Override
  public int getIndexForTab(SectionInfo info, String tabId) {
    TabContent tabContent = getVisibleTabsCached(info).get(tabId);
    if (tabContent == null) {
      return -1;
    }
    return tabContent.getIndex();
  }

  @Override
  public List<TabContent> getVisibleTabs(SectionInfo info) {
    return new ArrayList<TabContent>(getVisibleTabsCached(info).values());
  }

  public void addTabSection(TabSection section) {
    NameValue tab = section.getTabToAppearOn();
    List<TabSection> tabList = tabSections.get(tab.getValue());
    if (tabList != null) {
      tabList.add(section);
    } else {
      tabList = new ArrayList<TabSection>();
      tabList.add(section);
      tabSections.put(tab.getValue(), tabList);
    }
  }

  public void addTabSections(List<TabSection> sections) {
    for (TabSection tabSection : sections) {
      addTabSection(tabSection);
    }
  }

  public static class TabRenderer implements SectionRenderable {

    private SectionRenderable firstResult;
    private List<TabSection> sections;

    public TabRenderer(List<TabSection> sections) {
      this.sections = sections;
    }

    @Override
    public void realRender(SectionWriter writer) throws IOException {
      firstResult.realRender(writer);
    }

    @Override
    public void preRender(PreRenderContext info) {
      ResultListCollector listener = new ResultListCollector(true);
      for (TabSection tab : sections) {
        if (tab.isVisible(info)) {
          RenderEvent renderEvent = new RenderEvent(info, tab.getSectionId(), listener);
          info.processEvent(renderEvent);
        }
      }
      firstResult = listener.getFirstResult();
      info.preRender(firstResult);
    }
  }
}
