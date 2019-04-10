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

import com.tle.core.freetext.service.FreeTextService;
import com.tle.core.services.item.FreetextResult;
import com.tle.core.services.item.FreetextSearchResults;
import com.tle.core.services.user.UserSessionService;
import com.tle.web.integration.service.IntegrationService;
import com.tle.web.itemlist.item.StandardItemListEntry;
import com.tle.web.search.base.AbstractFreetextResultsSection;
import com.tle.web.search.base.AbstractSearchResultsSection.SearchResultsModel;
import com.tle.web.search.event.FreetextSearchEvent;
import com.tle.web.search.event.FreetextSearchResultEvent;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.search.AbstractSearchActionsSection;
import com.tle.web.sections.equella.search.AbstractSearchActionsSection.AbstractSearchActionsModel;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.render.Label;
import com.tle.web.selection.SelectionService;
import java.util.Collections;
import javax.inject.Inject;
import org.apache.log4j.Logger;

public class HierarchyResultsSection
    extends AbstractFreetextResultsSection<StandardItemListEntry, SearchResultsModel> {
  private static final Logger LOGGER = Logger.getLogger(HierarchyResultsSection.class);

  @Inject private HierarchyItemList itemList;
  @Inject private SelectionService selectionService;
  @Inject private IntegrationService integrationService;
  @Inject private FreeTextService freeText;
  @Inject private UserSessionService sessionService;

  @TreeLookup private TopicDisplaySection topicDisplay;

  @TreeLookup
  private AbstractSearchActionsSection<? extends AbstractSearchActionsModel> searchActionsSection;

  @Override
  public SectionResult renderHtml(RenderEventContext context) throws Exception {
    if (!topicDisplay.isShowingResults(context)) {
      searchActionsSection.disableSearch(context);
      return null;
    }
    return super.renderHtml(context);
  }

  @Override
  protected Label getDefaultResultsTitle(
      SectionInfo info, FreetextSearchEvent searchEvent, FreetextSearchResultEvent resultsEvent) {
    return topicDisplay.getResultsTitle(
        info, super.getDefaultResultsTitle(info, searchEvent, resultsEvent));
  }

  @SuppressWarnings("nls")
  @Override
  protected FreetextSearchResultEvent createResultsEvent(
      SectionInfo info, FreetextSearchEvent searchEvent) {
    int[] count =
        freeText.countsFromFilters(Collections.singleton(searchEvent.getUnfilteredSearch()));
    FreetextSearchResults<? extends FreetextResult> results =
        topicDisplay.processFreetextResults(info, searchEvent);
    return new FreetextSearchResultEvent(results, searchEvent, count[0] - results.getAvailable());
  }

  @Override
  public FreetextSearchEvent createSearchEvent(SectionInfo info) {
    return topicDisplay.createFreetextSearchEvent(info);
  }

  @Override
  public void processResults(SectionInfo info, FreetextSearchResultEvent event) {
    topicDisplay.processResults(info, event, itemList);
  }

  @Override
  protected void registerItemList(SectionTree tree, String id) {
    tree.registerInnerSection(itemList, id);
  }

  @Override
  public HierarchyItemList getItemList(SectionInfo info) {
    return itemList;
  }
}
