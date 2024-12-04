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

package com.tle.web.searching.prevnext;

import com.tle.beans.item.ItemKey;
import com.tle.core.freetext.service.FreeTextService;
import com.tle.core.guice.Bind;
import com.tle.core.services.user.UserSessionService;
import com.tle.web.search.base.AbstractFreetextResultsSection;
import com.tle.web.searching.SearchIndexModifier;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.viewable.impl.ViewableItemFactory;
import com.tle.web.viewitem.section.AbstractParentViewItemSection;
import com.tle.web.viewurl.ViewItemUrl;
import com.tle.web.viewurl.ViewItemUrlFactory;
import javax.inject.Inject;

@Bind
@SuppressWarnings("nls")
public class SearchPrevNextSection
    extends AbstractParentViewItemSection<SearchPrevNextSection.SearchPrevNextModel> {

  private static final int ONE_STEP_BACKWARDS = -1;
  private static final int ONE_STEP_FORWARDS = 1;

  @Component(name = "btnprev")
  @PlugKey("button.prev")
  private Button prevButton;

  @Component(name = "btnnext")
  @PlugKey("button.next")
  private Button nextButton;

  @EventFactory private EventGenerator events;

  @Inject private UserSessionService sessionService;
  @Inject private ViewItemUrlFactory urlFactory;
  @Inject private FreeTextService freetextService;
  @Inject private ViewableItemFactory viewableItemFactory;

  @Override
  public void registered(String id, SectionTree tree) {
    super.registered(id, tree);
    prevButton.setClickHandler(events.getNamedHandler("nav", ONE_STEP_BACKWARDS));
    nextButton.setClickHandler(events.getNamedHandler("nav", ONE_STEP_FORWARDS));
  }

  @EventHandlerMethod
  public void nav(SectionInfo info, int direction) {
    SearchPrevNextModel model = getModel(info);
    int nextIndex = model.getIndex() + direction;
    SectionInfo searchInfo = info.createForward(model.getSearchPage());
    AbstractFreetextResultsSection resultsSection =
        searchInfo.lookupSection(AbstractFreetextResultsSection.class);
    ItemKey resultKey = resultsSection.getResultForIndex(searchInfo, nextIndex);
    if (resultKey != null) {
      ViewItemUrl vurl = urlFactory.createItemUrl(info, resultKey);
      vurl.add(new SearchIndexModifier(model.getSearchPage(), nextIndex, model.getAvailable()));
      vurl.forward(info);
    }
  }

  @Override
  public boolean canView(SectionInfo info) {
    return getModel(info).getSearchPage() != null;
  }

  @Override
  public SectionResult renderHtml(RenderEventContext context) throws Exception {
    if (canView(context)) {
      SearchPrevNextModel model = getModel(context);
      int entryIndex = model.getIndex();
      if (entryIndex == 0) {
        prevButton.disable(context);
      }
      if (model.getAvailable() - 1 <= entryIndex) {
        nextButton.disable(context);
      }
      return viewFactory.createNamedResult("section_comments", "search_prev_next.ftl", this);
    }
    return null;
  }

  public Button getPrevButton() {
    return prevButton;
  }

  public Button getNextButton() {
    return nextButton;
  }

  @Override
  public String getDefaultPropertyName() {
    return "prevnextbuttons";
  }

  @Override
  public Class<SearchPrevNextModel> getModelClass() {
    return SearchPrevNextModel.class;
  }

  public static class SearchPrevNextModel {

    @Bookmarked(parameter = "search")
    private String searchPage;

    @Bookmarked(parameter = "index")
    private int index;

    @Bookmarked(parameter = "available")
    private int available;

    public int getIndex() {
      return index;
    }

    public void setIndex(int index) {
      this.index = index;
    }

    public void setSearchPage(String searchPage) {
      this.searchPage = searchPage;
    }

    public String getSearchPage() {
      return searchPage;
    }

    public int getAvailable() {
      return available;
    }

    public void setAvailable(int available) {
      this.available = available;
    }
  }
}
