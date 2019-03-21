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

package com.tle.web.searching.prevnext;

import com.tle.core.freetext.service.FreeTextService;
import com.tle.core.guice.Bind;
import com.tle.core.services.user.UserSessionService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.viewable.impl.ViewableItemFactory;
import com.tle.web.viewitem.section.AbstractParentViewItemSection;
import com.tle.web.viewurl.ViewItemUrlFactory;
import javax.inject.Inject;

/** @author larry */
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
  public void nav(SectionInfo info, int direction) {}

  @Override
  public boolean canView(SectionInfo info) {
    return true;
  }

  @Override
  public SectionResult renderHtml(RenderEventContext context) throws Exception {
    if (canView(context)) {
      SearchPrevNextModel model = getModel(context);
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

  public static class SearchPrevNextModel {}
}
