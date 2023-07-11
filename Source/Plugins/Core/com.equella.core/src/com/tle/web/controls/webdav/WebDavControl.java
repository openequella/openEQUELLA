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
import com.tle.core.guice.Bind;
import com.tle.core.wizard.LERepository;
import com.tle.web.core.servlet.webdav.WebDavAuthService;
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
import javax.inject.Inject;
import scala.Tuple2;

@SuppressWarnings("nls")
@Bind
public class WebDavControl extends AbstractWebControl<WebDavControl.WebDavControlModel> {
  @ViewFactory(name = "wizardFreemarkerFactory")
  private FreemarkerFactory factory;

  @Inject private WebDavAuthService webDavAuthService;

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
    if (isAutoMarkAsResource()) {
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
    WebRepository repository = (WebRepository) control.getRepository();
    String webdav = repository.getWebUrl() + "wd/" + repository.getStagingid() + '/';

    Tuple2<String, String> creds = webDavAuthService.createCredentials(repository.getStagingid());

    WebDavControlModel model = getModel(context);
    model.setWebdavUrl(webdav);
    model.setWebdavUsername(creds._1());
    model.setWebdavPassword(creds._2());
  }

  @Override
  public Class<WebDavControlModel> getModelClass() {
    return WebDavControlModel.class;
  }

  public Button getRefreshButton() {
    return refreshButton;
  }

  public SelectionsTable getFilesTable() {
    return filesTable;
  }

  @Override
  protected ElementId getIdForLabel() {
    return null;
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

  public static class WebDavControlModel extends WebControlModel {
    private String webdavUrl;
    private String webdavUsername;
    private String webdavPassword;

    public String getWebdavUrl() {
      return webdavUrl;
    }

    public void setWebdavUrl(String webdavUrl) {
      this.webdavUrl = webdavUrl;
    }

    public String getWebdavUsername() {
      return webdavUsername;
    }

    public void setWebdavUsername(String webdavUsername) {
      this.webdavUsername = webdavUsername;
    }

    public String getWebdavPassword() {
      return webdavPassword;
    }

    public void setWebdavPassword(String webdavPassword) {
      this.webdavPassword = webdavPassword;
    }
  }
}
