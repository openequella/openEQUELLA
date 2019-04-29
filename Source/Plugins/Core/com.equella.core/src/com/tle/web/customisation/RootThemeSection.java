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

package com.tle.web.customisation;

import com.tle.common.filesystem.FileEntry;
import com.tle.common.filesystem.handle.StagingFile;
import com.tle.core.filesystem.CustomisationFile;
import com.tle.core.filesystem.staging.service.StagingService;
import com.tle.core.guice.Bind;
import com.tle.core.services.FileSystemService;
import com.tle.core.util.archive.ArchiveType;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionContext;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.ajax.handler.AjaxMethod;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.DirectEvent;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.ajaxupload.AjaxUpload;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.layout.OneColumnLayout;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.SectionEvent;
import com.tle.web.sections.events.js.BookmarkAndModify;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.js.JSAssignable;
import com.tle.web.sections.js.generic.function.AnonymousFunction;
import com.tle.web.sections.js.generic.statement.ReloadStatement;
import com.tle.web.sections.render.GenericTemplateResult;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.FileUpload;
import com.tle.web.sections.standard.Link;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.settings.menu.SettingsUtils;
import com.tle.web.template.Breadcrumbs;
import com.tle.web.template.Decorations;
import com.tle.web.template.section.HelpAndScreenOptionsSection;
import java.io.IOException;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

@Bind
public class RootThemeSection
    extends AbstractPrototypeSection<RootThemeSection.RootCustomisationModel>
    implements HtmlRenderer {
  @PlugKey("customisation.title")
  private static Label TITLE_LABEL;

  @EventFactory private EventGenerator events;
  @ViewFactory private FreemarkerFactory viewFactory;
  @AjaxFactory protected AjaxGenerator ajax;

  @Inject private FileSystemService fileSystemService;
  @Inject private StagingService stagingService;
  @Inject private ThemePrivilegeTreeProvider securityProvider;

  @Component
  @PlugKey("download")
  private Link download;

  @Component
  @PlugKey("theme.delete")
  private Link delete;

  @Component private FileUpload upload;
  private JSAssignable validateFile;

  @Override
  public void registered(String id, SectionTree tree) {
    super.registered(id, tree);

    delete.setEventHandler("click", events.getNamedHandler("delete"));
    validateFile =
        AjaxUpload.simpleUploadValidator("uploads", new AnonymousFunction(new ReloadStatement()));
  }

  @DirectEvent(priority = SectionEvent.PRIORITY_BEFORE_EVENTS)
  public void checkAuthorised(SectionInfo info) {
    securityProvider.checkAuthorised();
  }

  @DirectEvent
  public void prepare(SectionContext context) throws IOException {
    RootCustomisationModel model = getModel(context);
    CustomisationFile baseCustomisations = new CustomisationFile();

    FileEntry[] entries = fileSystemService.enumerate(baseCustomisations, "", null);
    model.setExistsCurrentTheme(entries.length > 0);
  }

  @Override
  public SectionResult renderHtml(RenderEventContext context) throws IOException {
    upload.setAjaxUploadUrl(context, ajax.getAjaxUrl(context, "upload"));
    upload.setValidateFile(context, validateFile);
    RootCustomisationModel model = getModel(context);
    if (model.isDoDownload()) {
      context.setRendered();
      HttpServletResponse response = context.getResponse();
      response.setContentType("application/zip");
      response.setHeader("Content-Disposition", "inline; filename=export.zip");
      fileSystemService.zipFile(
          new CustomisationFile(), response.getOutputStream(), ArchiveType.ZIP);
      return null;
    }

    Decorations.getDecorations(context).setTitle(TITLE_LABEL);
    Breadcrumbs.get(context).add(SettingsUtils.getBreadcrumb(context));

    GenericTemplateResult gtr = new GenericTemplateResult();
    HelpAndScreenOptionsSection.addHelp(
        context, viewFactory.createResult("themesettingshelp.ftl", this));

    if (model.isExistsCurrentTheme()) {
      download.setBookmark(
          context, new BookmarkAndModify(context, events.getNamedModifier("download")));
      gtr.addNamedResult(
          OneColumnLayout.BODY, viewFactory.createResult("currentcustomisation.ftl", context));
    } else {
      gtr.addNamedResult(
          OneColumnLayout.BODY, viewFactory.createResult("uploadcustomisation.ftl", context));
    }
    return gtr;
  }

  @AjaxMethod
  public boolean upload(SectionInfo info) throws IOException {
    StagingFile staging = stagingService.createStagingArea();
    fileSystemService.unzipFile(staging, upload.getInputStream(info), ArchiveType.ZIP);
    fileSystemService.commitFiles(staging, new CustomisationFile());
    return true;
  }

  @EventHandlerMethod
  public void download(SectionContext context) {
    RootCustomisationModel model = getModel(context);
    model.setDoDownload(true);
  }

  @EventHandlerMethod
  public void delete(SectionContext context) {
    CustomisationFile file = new CustomisationFile();
    fileSystemService.removeFile(file, null);
  }

  public Link getDownload() {
    return download;
  }

  public Link getDelete() {
    return delete;
  }

  public FileUpload getUpload() {
    return upload;
  }

  @Override
  public Class<RootCustomisationModel> getModelClass() {
    return RootCustomisationModel.class;
  }

  public static class RootCustomisationModel {
    @Bookmarked private boolean existsCurrentTheme;
    @Bookmarked private boolean doDownload;

    public boolean isExistsCurrentTheme() {
      return existsCurrentTheme;
    }

    public void setExistsCurrentTheme(boolean existsCurrentTheme) {
      this.existsCurrentTheme = existsCurrentTheme;
    }

    public boolean isDoDownload() {
      return doDownload;
    }

    public void setDoDownload(boolean doDownload) {
      this.doDownload = doDownload;
    }
  }
}
