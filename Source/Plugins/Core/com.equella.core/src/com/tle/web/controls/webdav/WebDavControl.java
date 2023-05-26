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

package com.tle.web.controls.webdav;

import com.dytech.edge.wizard.beans.control.CustomControl;
import com.tle.beans.item.attachments.AttachmentType;
import com.tle.beans.item.attachments.FileAttachment;
import com.tle.beans.item.attachments.ModifiableAttachments;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.guice.Bind;
import com.tle.core.wizard.LERepository;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionContext;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.equella.component.SelectionsTable;
import com.tle.web.sections.equella.component.model.DynamicSelectionsTableModel;
import com.tle.web.sections.equella.component.model.SelectionsTableSelection;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.js.ElementId;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.renderers.LinkRenderer;
import com.tle.web.wizard.controls.AbstractWebControl;
import com.tle.web.wizard.controls.WebControlModel;
import com.tle.web.wizard.impl.WebRepository;
import java.util.List;

@SuppressWarnings("nls")
@Bind
public class WebDavControl extends AbstractWebControl<WebControlModel> {
  @ViewFactory(name = "wizardFreemarkerFactory")
  private FreemarkerFactory factory;

  @Component private Button openWebdav;
  @Component private Button refreshButton;

  @Component(name = "f")
  private SelectionsTable filesTable;

  @Override
  public SectionResult renderHtml(RenderEventContext context) throws Exception {
    setupWebdavUrl(context);
    return factory.createResult("webdav/webdav.ftl", context);
  }

  private List<FileAttachment> getFiles() {
    LERepository repo = getRepository();
    ModifiableAttachments attachments = repo.getAttachments();

    return attachments.getList(AttachmentType.FILE);
  }

  @Override
  public boolean isEmpty() {
    return getFiles().isEmpty();
  }

  @Override
  public void doEdits(SectionInfo info) {
    if (isAutoMarkAsResource() && isWebdav()) {
      WebRepository repository = (WebRepository) control.getRepository();
      repository.selectTopLevelFilesAsAttachments();
    }
  }

  @Override
  public void registered(String id, SectionTree tree) {
    super.registered(id, tree);

    refreshButton.setClickHandler(getReloadFunction(true, null));

    filesTable.setSelectionsModel(new FilesModel());
  }

  public boolean isAutoMarkAsResource() {
    CustomControl c = (CustomControl) getControlBean();
    Boolean b = (Boolean) c.getAttributes().get("autoMarkAsResource");
    return b == null || b.booleanValue();
  }

  private void setupWebdavUrl(SectionContext context) {
    if (isWebdav()) {
      WebRepository repository = (WebRepository) control.getRepository();
      String webdav = repository.getWebUrl() + "wd/" + repository.getStagingid() + '/';

      openWebdav.setClickHandler(
          context,
          new OverrideHandler(
              new ExternallyDefinedFunction("openWebDav"),
              getSectionId(),
              CurrentLocale.get("wizard.controls.file.url", webdav),
              webdav));
      addDisablers(context, openWebdav, refreshButton);
    } else {
      openWebdav.setDisplayed(context, false);
    }
  }

  public boolean isWebdav() {
    CustomControl c = (CustomControl) getControlBean();
    Boolean b = (Boolean) c.getAttributes().get("allowWebDav");
    return b == null || b.booleanValue();
  }

  @Override
  public Class<WebControlModel> getModelClass() {
    return WebControlModel.class;
  }

  public Button getOpenWebdav() {
    return openWebdav;
  }

  public Button getRefreshButton() {
    return refreshButton;
  }

  public SelectionsTable getFilesTable() {
    return filesTable;
  }

  private class FilesModel extends DynamicSelectionsTableModel<FileAttachment> {
    @Override
    protected List<FileAttachment> getSourceList(SectionInfo info) {
      return getFiles();
    }

    @Override
    protected void transform(
        SectionInfo info,
        SelectionsTableSelection selection,
        FileAttachment attachment,
        List<SectionRenderable> actions,
        int index) {
      final WebRepository repository = getWebRepository();
      final HtmlLinkState view = new HtmlLinkState(repository.getFileURL(attachment.getFilename()));
      final LinkRenderer viewLink = new LinkRenderer(view);
      viewLink.setLabel(new TextLabel(attachment.getDescription()));
      viewLink.setTarget("_blank");
      selection.setViewAction(viewLink);
    }
  }

  @Override
  protected ElementId getIdForLabel() {
    return null;
  }
}
