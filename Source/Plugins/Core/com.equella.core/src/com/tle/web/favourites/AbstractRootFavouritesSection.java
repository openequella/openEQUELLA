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

package com.tle.web.favourites;

import com.tle.web.search.base.ContextableSearchSection;
import com.tle.web.sections.MutableSectionInfo;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.utils.VoidKeyOption;
import com.tle.web.sections.events.ParametersEvent;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.SimpleHtmlListModel;
import java.util.List;

@SuppressWarnings("nls")
public abstract class AbstractRootFavouritesSection
    extends ContextableSearchSection<AbstractRootFavouritesSection.Model> {
  private static final String CONTEXT_KEY = "favouritesContext";

  private static final String SEARCHTYPE_PARAM = "favtype";
  private static final String SEARCHES_TYPE = "searches";
  private static final String ITEMS_TYPE = "items";

  @PlugKey("favourites.title")
  private static Label LABEL_TITLE;

  @PlugKey(SEARCHES_TYPE)
  private static String KEY_SEARCHES;

  @PlugKey(ITEMS_TYPE)
  private static String KEY_ITEMS;

  @Component(stateful = false)
  private SingleSelectionList<Void> favouriteType;

  protected abstract SectionTree getSearchTree();

  protected abstract SectionTree getItemTree();

  @EventHandlerMethod
  public void changeSearchType(SectionInfo info) {
    getModel(info).setSearchType(favouriteType.getSelectedValueAsString(info));
  }

  @EventFactory private EventGenerator events;

  @Override
  public void registered(String id, SectionTree tree) {
    super.registered(id, tree);

    favouriteType.setListModel(
        new SimpleHtmlListModel<Void>(
            new VoidKeyOption(KEY_ITEMS, ITEMS_TYPE),
            new VoidKeyOption(KEY_SEARCHES, SEARCHES_TYPE)));
    favouriteType.addChangeEventHandler(events.getNamedHandler("changeSearchType"));
    favouriteType.setAlwaysSelect(true);
  }

  @Override
  protected String getSessionKey() {
    return CONTEXT_KEY;
  }

  @Override
  public Label getTitle(SectionInfo info) {
    return LABEL_TITLE;
  }

  public Label getHeaderTitle() {
    return LABEL_TITLE;
  }

  @Override
  protected boolean hasContextBeenSpecified(SectionInfo info) {
    return getModel(info).isUpdateContext();
  }

  @Override
  public void afterParameters(SectionInfo info, ParametersEvent event) {
    if (event.hasParameter(SEARCHTYPE_PARAM)) {
      if (!event.isInitial()) {
        MutableSectionInfo minfo = info.getAttributeForClass(MutableSectionInfo.class);
        minfo.addParametersEvent(event);
      }
      getCurrentTree(info, true);
    }
    super.afterParameters(info, event);
  }

  @Override
  protected List<SectionId> getChildIds(RenderContext info) {
    SectionTree tree = getCurrentTree(info, false);
    return tree.getChildIds(tree.getRootId());
  }

  private SectionTree getCurrentTree(SectionInfo info, boolean params) {
    Model model = getModel(info);
    String list = model.getSearchType();
    SectionTree tree = list.equals(SEARCHES_TYPE) ? getSearchTree() : getItemTree();
    if (!model.treeAdded) {
      model.setTreeAdded(true);
      MutableSectionInfo minfo = info.getAttributeForClass(MutableSectionInfo.class);
      minfo.addTreeToBottom(tree, params);
    }
    return tree;
  }

  @Override
  protected SectionRenderable getBodyHeader(RenderContext info) {
    favouriteType.setSelectedStringValue(info, getModel(info).getSearchType());
    return viewFactory.createResult("favourites.ftl", this);
  }

  public SingleSelectionList<Void> getFavouriteType() {
    return favouriteType;
  }

  @Override
  public Object instantiateModel(SectionInfo info) {
    return new Model();
  }

  @Override
  protected String getPageName() {
    return "/access/favourites.do";
  }

  public static class Model extends ContextableSearchSection.Model {

    @Bookmarked(parameter = SEARCHTYPE_PARAM)
    private String searchType = ITEMS_TYPE;

    private boolean treeAdded;

    public boolean isTreeAdded() {
      return treeAdded;
    }

    public void setTreeAdded(boolean treeAdded) {
      this.treeAdded = treeAdded;
    }

    public String getSearchType() {
      return searchType;
    }

    public void setSearchType(String searchType) {
      this.searchType = searchType;
    }
  }
}
