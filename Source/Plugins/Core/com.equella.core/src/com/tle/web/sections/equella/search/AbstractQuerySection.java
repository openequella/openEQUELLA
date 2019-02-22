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

package com.tle.web.sections.equella.search;

import com.google.common.base.Strings;
import com.tle.annotation.NonNullByDefault;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.TreeIndexed;
import com.tle.web.sections.equella.search.event.AbstractSearchEvent;
import com.tle.web.sections.equella.search.event.SearchEventListener;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.renderers.LabelTagRenderer;

@NonNullByDefault
@TreeIndexed
public abstract class AbstractQuerySection<M, SE extends AbstractSearchEvent<SE>>
    extends AbstractPrototypeSection<M> implements SearchEventListener<SE>, HtmlRenderer {
  /* UI */
  @Component(name = "qf", parameter = "q", supported = true)
  protected TextField queryField;

  @Component(name = "s")
  protected Button searchButton;

  @ViewFactory(fixed = false)
  protected FreemarkerFactory viewFactory;

  @Override
  public void registered(String id, SectionTree tree) {
    super.registered(id, tree);
    getQueryField().setDontBookmarkBlank(false);
  }

  public TextField getQueryField() {
    return queryField;
  }

  public Button getSearchButton() {
    return searchButton;
  }

  @Override
  public void prepareSearch(SectionInfo info, SE event) throws Exception {
    if (!event.isExcludeKeywords()) {
      event.filterByTextQuery(getParsedQuery(info), isIncludeUnfiltered());
    }
  }

  protected boolean isIncludeUnfiltered() {
    return true;
  }

  public String getParsedQuery(SectionInfo info) {
    return parseQuery(getQueryField().getValue(info));
  }

  protected String parseQuery(String query) {
    return Strings.nullToEmpty(query);
  }

  public void setQuery(SectionInfo info, String query) {
    getQueryField().setValue(info, query);
  }

  public LabelTagRenderer getLabelTag() {
    return new LabelTagRenderer(queryField, null, null);
  }
}
