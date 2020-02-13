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

package com.tle.web.template;

import com.tle.core.guice.Bind;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.js.ElementId;
import com.tle.web.sections.render.FallbackTemplateResult;
import com.tle.web.sections.render.GenericTemplateResult;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.LabelRenderer;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TagRenderer;
import com.tle.web.sections.render.TemplateRenderable;
import com.tle.web.sections.render.TemplateResult;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.dialog.AbstractDialog;
import com.tle.web.sections.standard.model.HtmlComponentState;
import com.tle.web.sections.standard.renderers.ImageButtonRenderer;
import javax.inject.Inject;
import javax.inject.Singleton;

@Bind
@Singleton
@SuppressWarnings("nls")
public class DialogTemplate {
  private static final PluginResourceHelper resources =
      ResourcesService.getResourceHelper(DialogTemplate.class);
  private static Label LABEL_CLOSE = new KeyLabel(resources.key("dialog.button.close"));
  private static String CLOSE_PNG_URL = resources.url("images/dialog/dialog_close.png");

  public static final String BODY = "body";
  public static final String FOOTER = "footer";

  @Inject private FreemarkerFactory viewFactory;

  public SectionRenderable getLayout(
      Label title,
      RenderContext info,
      TemplateResult result,
      AbstractDialog<?> dialog,
      JSHandler closeHandler,
      String contentBodyClass,
      ElementId footerId,
      ElementId titleId) {
    if (dialog.isModal()) {
      HtmlComponentState closeDialog = new HtmlComponentState(closeHandler);
      closeDialog.setId(dialog.getElementId(info) + "_close");
      final ImageButtonRenderer renderer = new ImageButtonRenderer(closeDialog);
      renderer.addClass("modal_close");
      renderer.setSource(CLOSE_PNG_URL);
      renderer.setNestedRenderable(new LabelRenderer(LABEL_CLOSE));
      result =
          new FallbackTemplateResult(
              result, new GenericTemplateResult().addNamedResult("head", renderer));
    }

    DialogTemplateModel model = new DialogTemplateModel();
    model.setPageTitle(new LabelRenderer(title));
    model.setFooterId(footerId.getElementId(info));
    model.setTitleId(titleId.getElementId(info));
    model.setTemplate(result);
    model.setContentBodyClass(contentBodyClass);

    TemplateRenderable footer = result.getNamedResult(info, FOOTER);
    if (footer != null && footer.exists(info)) {
      model.setFooter(footer);
    }
    dialog.getState(info).setAccessibilityAttr(TagRenderer.ARIA_LABELLEDBY, model.getTitleId());
    return viewFactory.createResultWithModel("layouts/inner/dialog.ftl", model);
  }

  public static class DialogTemplateModel {
    private TemplateResult template;
    private SectionRenderable pageTitle;
    private boolean modal;
    private String contentBodyClass;
    private TemplateRenderable footer;
    private String footerId;
    private String titleId;

    public String getContentBodyClass() {
      return contentBodyClass;
    }

    public void setContentBodyClass(String contentBodyClass) {
      this.contentBodyClass = contentBodyClass;
    }

    public TemplateResult getTemplate() {
      return template;
    }

    public void setTemplate(TemplateResult template) {
      this.template = template;
    }

    public SectionRenderable getPageTitle() {
      return pageTitle;
    }

    public void setPageTitle(SectionRenderable pageTitle) {
      this.pageTitle = pageTitle;
    }

    public boolean isModal() {
      return modal;
    }

    public void setModal(boolean modal) {
      this.modal = modal;
    }

    public TemplateRenderable getFooter() {
      return footer;
    }

    public void setFooter(TemplateRenderable footer) {
      this.footer = footer;
    }

    public String getFooterId() {
      return footerId;
    }

    public void setFooterId(String footerId) {
      this.footerId = footerId;
    }

    public String getTitleId() {
      return titleId;
    }

    public void setTitleId(String titleId) {
      this.titleId = titleId;
    }
  }
}
