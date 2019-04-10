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

package com.tle.web.mimetypes.search.section;

import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.mimetypes.MimeSearchPrivilegeTreeProvider;
import com.tle.web.search.base.ContextableSearchSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.annotations.DirectEvent;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.annotation.PlugURL;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.render.CssInclude;
import com.tle.web.sections.render.Label;
import com.tle.web.settings.menu.SettingsUtils;
import com.tle.web.template.Breadcrumbs;
import com.tle.web.template.Decorations;
import com.tle.web.template.section.event.BlueBarEvent;
import com.tle.web.template.section.event.BlueBarEventListener;
import java.util.List;
import javax.inject.Inject;

@SuppressWarnings("nls")
public class RootMimeSection extends ContextableSearchSection<ContextableSearchSection.Model>
    implements BlueBarEventListener {
  @PlugURL("css/mime.css")
  private static String CSS_URL;

  @PlugKey("mimetypes.title")
  private static Label TITLE_LABEL;

  @Inject private MimeSearchPrivilegeTreeProvider securityProvider;

  @ViewFactory private FreemarkerFactory view;

  @Override
  protected String getSessionKey() {
    return "mimeContext";
  }

  @DirectEvent
  public void ensurePrivs(SectionInfo info) {
    securityProvider.checkAuthorised();
  }

  @Override
  protected void createCssIncludes(List<CssInclude> includes) {
    includes.add(CssInclude.include(CSS_URL).hasRtl().make());
    super.createCssIncludes(includes);
  }

  @Override
  public Label getTitle(SectionInfo info) {
    return TITLE_LABEL;
  }

  @Override
  protected void addBreadcrumbsAndTitle(
      SectionInfo info, Decorations decorations, Breadcrumbs crumbs) {
    super.addBreadcrumbsAndTitle(info, decorations, crumbs);

    Breadcrumbs.get(info).add(SettingsUtils.getBreadcrumb(info));
  }

  @Override
  protected String getContentBodyClasses() {
    return super.getContentBodyClasses() + " mimetypes";
  }

  @Override
  public void addBlueBarResults(RenderContext context, BlueBarEvent event) {
    event.addHelp(view.createResult("mime-help.ftl", this));
  }

  @Override
  protected String getPageName() {
    return null;
  }
}
