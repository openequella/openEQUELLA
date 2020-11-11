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

package com.tle.web.search.base;

import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.TreeIndexed;
import com.tle.web.sections.equella.annotation.PlugURL;
import com.tle.web.sections.equella.layout.OneColumnLayout;
import com.tle.web.sections.equella.layout.TwoColumnLayout;
import com.tle.web.sections.events.BookmarkEvent;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.generic.InfoBookmark;
import com.tle.web.sections.render.CombinedRenderer;
import com.tle.web.sections.render.CombinedTemplateResult;
import com.tle.web.sections.render.CssInclude;
import com.tle.web.sections.render.GenericTemplateResult;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.SimpleSectionResult;
import com.tle.web.sections.render.TemplateResult;
import com.tle.web.template.Breadcrumbs;
import com.tle.web.template.Decorations;
import com.tle.web.template.section.event.BlueBarEvent;
import java.util.ArrayList;
import java.util.List;

@TreeIndexed
public abstract class AbstractRootSearchSection<M extends AbstractRootSearchSection.Model>
    extends TwoColumnLayout<M> {
  @PlugURL("css/search.css")
  private static String URL_CSS;

  private CssInclude[] cssIncludes;

  @Override
  protected void addBreadcrumbsAndTitle(
      SectionInfo info, Decorations decorations, Breadcrumbs crumbs) {
    decorations.setTitle(getTitle(info));
    decorations.setContentBodyClass(getContentBodyClasses());
  }

  @SuppressWarnings("nls")
  protected String getContentBodyClasses() {
    return "search-layout";
  }

  @Override
  public void registered(String id, SectionTree tree) {
    super.registered(id, tree);
    List<CssInclude> includes = new ArrayList<CssInclude>();
    createCssIncludes(includes);
    cssIncludes = includes.toArray(new CssInclude[includes.size()]);
  }

  protected void createCssIncludes(List<CssInclude> includes) {
    includes.add(CssInclude.include(URL_CSS).hasRtl().make());
  }

  @Override
  protected TemplateResult getTemplateResult(RenderEventContext info) {
    CombinedTemplateResult templateResult = new CombinedTemplateResult();
    M model = getModel(info);
    SectionId modalSection = model.getModalSection();
    SimpleSectionResult newSearchUIContent = model.getNewSearchUIContent();

    if (modalSection != null) {
      templateResult.addNamedResult(
          OneColumnLayout.BODY, CombinedRenderer.combineMultipleResults(cssIncludes));
      templateResult.addResult(
          OneColumnLayout.BODY, SectionUtils.renderSectionResult(info, modalSection));
    }

    // The only case where newSearchUIContent is NOT null is rendering the new Search UI
    // in selection session and selection session is using the NORMAL layout. This layout
    // contains two named columns: left and right.
    else if (newSearchUIContent != null) {
      // Firstly, find out child sections which use 'TwoColumnLayout.RIGHT', and
      // add their contents to the 'right' column.
      // Secondly, add the new search UI content with a name being 'body' to the template,
      // and 'body' will get merged to 'left' column later in method 'setupTemplate'.

      getChildIds(info).stream()
          .filter(childId -> TwoColumnLayout.RIGHT.equals(info.getLayout(childId.getSectionId())))
          .forEach(
              childId ->
                  templateResult.addResult(
                      TwoColumnLayout.RIGHT, SectionUtils.renderSectionResult(info, childId)));

      templateResult.addNamedResult(OneColumnLayout.BODY, newSearchUIContent);
    } else {
      SectionRenderable bodyHeader = getBodyHeader(info);
      if (bodyHeader != null) {
        templateResult.addNamedResult(OneColumnLayout.BODY, bodyHeader);
      }
      List<SectionId> children = getChildIds(info);
      for (SectionId childId : children) {
        String side = info.getLayout(childId.getSectionId());

        if (!TwoColumnLayout.RIGHT.equals(side)) {
          side = OneColumnLayout.BODY;
        }
        templateResult.addResult(side, SectionUtils.renderSectionResult(info, childId));
      }
      templateResult.addNamedResult(
          OneColumnLayout.BODY, CombinedRenderer.combineMultipleResults(cssIncludes));

      addBlueBarBits(info, templateResult);
    }

    return templateResult;
  }

  protected SectionRenderable getBodyHeader(RenderContext info) {
    return null;
  }

  protected void addBlueBarBits(RenderContext info, GenericTemplateResult templateResult) {
    BlueBarEvent blueBarEvent = new BlueBarEvent(info);
    info.processEvent(blueBarEvent);
  }

  protected List<SectionId> getChildIds(RenderContext info) {
    return info.getChildIds(this);
  }

  public InfoBookmark getPermanentUrl(SectionInfo info) {
    M model = getModel(info);
    InfoBookmark permanentUrl = model.getPermanentUrl();
    if (permanentUrl == null) {
      BookmarkEvent bookmarkEvent = new BookmarkEvent(BookmarkEvent.CONTEXT_SUPPORTED);
      bookmarkEvent.setIgnoredContexts(BookmarkEvent.CONTEXT_SESSION);
      permanentUrl = new InfoBookmark(info, bookmarkEvent);
      model.setPermanentUrl(permanentUrl);
    }
    return permanentUrl;
  }

  public abstract Label getTitle(SectionInfo info);

  @Override
  public Object instantiateModel(SectionInfo info) {
    return new Model();
  }

  public static class Model extends TwoColumnLayout.TwoColumnModel {
    private InfoBookmark permanentUrl;
    private SimpleSectionResult newSearchUIContent;

    public InfoBookmark getPermanentUrl() {
      return permanentUrl;
    }

    public void setPermanentUrl(InfoBookmark permanentUrl) {
      this.permanentUrl = permanentUrl;
    }

    public SimpleSectionResult getNewSearchUIContent() {
      return newSearchUIContent;
    }

    public void setNewSearchUIContent(SimpleSectionResult newSearchUIContent) {
      this.newSearchUIContent = newSearchUIContent;
    }
  }
}
