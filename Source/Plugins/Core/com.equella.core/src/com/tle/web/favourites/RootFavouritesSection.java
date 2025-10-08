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

import com.tle.core.guice.Bind;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.equella.layout.ContentLayout;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.render.TextUtils;
import com.tle.web.template.RenderNewSearchPage;
import com.tle.web.template.RenderNewTemplate;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Named;

@Bind
public class RootFavouritesSection extends AbstractRootFavouritesSection {
  public static final String SEARCH_TREE_NAME = "searchTree";
  public static final String ITEM_TREE_NAME = "itemTree";

  @Inject
  @Named(SEARCH_TREE_NAME)
  private SectionTree searchTree;

  @Inject
  @Named(ITEM_TREE_NAME)
  private SectionTree itemTree;

  @Override
  protected SectionTree getSearchTree() {
    return searchTree;
  }

  @Override
  protected SectionTree getItemTree() {
    return itemTree;
  }

  @Override
  protected ContentLayout getDefaultLayout(SectionInfo info) {
    return selectionService.getCurrentSession(info) != null
        ? super.getDefaultLayout(info)
        : ContentLayout.ONE_COLUMN;
  }

  @Override
  public SectionResult renderHtml(RenderEventContext context) {
    if (RenderNewTemplate.isNewUIEnabled()) {
      getModel(context).setNewUIContent(RenderNewSearchPage.renderNewFavouritesPage(context));
    }
    return super.renderHtml(context);
  }

  /**
   * Generate Favourite Item tags by tokenizing a provided string. Each tag will be converted to
   * lowercase and stored in a Set.
   *
   * @param tagString A string where each tag is separated by a space, a comma, or a semi-colon
   * @return A set of tokenized tags
   */
  public static Set<String> tagsFromString(String tagString) {
    return TextUtils.tokenizeString(tagString, "\\s|,|;")
        .map(String::toLowerCase)
        .collect(Collectors.toSet());
  }
}
