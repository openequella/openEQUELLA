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

package com.tle.web.sections.equella.render;

import com.tle.common.i18n.CurrentLocale;
import com.tle.core.guice.Bind;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.Bookmark;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.SectionsRuntimeException;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.js.generic.AppendedElementId;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.js.generic.expression.ObjectExpression;
import com.tle.web.sections.js.generic.function.AnonymousFunction;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.render.*;
import com.tle.web.sections.standard.RendererFactory;
import com.tle.web.sections.standard.RendererFactoryExtension;
import com.tle.web.sections.standard.model.HtmlComponentState;
import com.tle.web.sections.standard.model.HtmlFileUploadState;
import java.io.IOException;
import java.util.Map;
import javax.inject.Singleton;

/** plugin defines the stateClassName as HtmlFileUploadState */
@Bind
@Singleton
@SuppressWarnings("nls")
public class EquellaFileUploadExtension implements RendererFactoryExtension {
  private static final PluginResourceHelper RESOURCES =
      ResourcesService.getResourceHelper(EquellaFileUploadExtension.class);

  public static final CssInclude CSS =
      CssInclude.include(RESOURCES.url("css/render/jquery.fileinput.css")).hasRtl().make();
  private static final IncludeFile JS =
      new IncludeFile(RESOURCES.url("scripts/render/jquery.fileinput.js"));

  private static final ExternallyDefinedFunction INIT =
      new ExternallyDefinedFunction("setupUpload", JS);

  private static final String BROWSE_KEY = RESOURCES.key("equellafileupload.browse");
  private static final String CHANGE_KEY = RESOURCES.key("equellafileupload.change");
  private static final String NONE_SELECTED_KEY = RESOURCES.key("equellafileupload.noneselected");

  @Override
  public SectionRenderable getRenderer(
      RendererFactory rendererFactory,
      SectionInfo info,
      String renderer,
      HtmlComponentState state) {
    return new FancyFileRenderer((HtmlFileUploadState) state); // NOSONAR
  }

  public static class FancyFileRenderer extends TagRenderer {
    private final HtmlFileUploadState uploadState;
    private boolean renderFile = true;
    private boolean renderBar = true;
    private int size;

    protected FancyFileRenderer(HtmlFileUploadState state) {
      super("div", new TagState(new AppendedElementId(state, "div")));
      addClass("customfile");
      this.uploadState = state;
    }

    public void setParts(boolean bar, boolean file) {
      renderBar = bar;
      renderFile = file;
    }

    public void setSize(int size) {
      this.size = size;
    }

    @Override
    protected void prepareFirstAttributes(SectionWriter writer, Map<String, String> attrs)
        throws IOException {
      super.prepareFirstAttributes(writer, attrs);
    }

    @Override
    protected void writeMiddle(SectionWriter writer) throws IOException {
      super.writeMiddle(writer);
      writer.writeTag(
          "button", "class", "customfile-button focus " + EquellaButtonExtension.CLASS_BUTTON);
      writer.writeText(CurrentLocale.get(BROWSE_KEY));
      writer.endTag("button");
      writer.writeTag("span", "class", "customfile-feedback");
      writer.writeText(CurrentLocale.get(NONE_SELECTED_KEY));
      writer.endTag("span");
      TagState tagState = new TagState(uploadState);
      tagState.addTagProcessor(
          new ExtraAttributes(
              "type", "file", "tabIndex", "-1", "name", uploadState.getElementId(writer)));
      TagRenderer fileTag = new TagRenderer("input", tagState);
      fileTag.addClass("customfile-input");
      writer.render(fileTag);
    }

    @Override
    public void preRender(PreRenderContext info) {
      super.preRender(info);

      if (renderFile) {
        info.preRender(CSS);
        if (!uploadState.isDontInitialise()) {
          ObjectExpression oe = new ObjectExpression();
          JSHandler onChange = uploadState.getHandler(JSHandler.EVENT_CHANGE);
          if (onChange != null) {
            oe.put("onchange", new AnonymousFunction(onChange));
          }
          Bookmark ajaxUploadUrl = uploadState.getAjaxUploadUrl();
          if (ajaxUploadUrl != null) {
            oe.put("ajaxUploadUrl", ajaxUploadUrl.getHref());
            oe.put("validateFile", uploadState.getValidateFile());
          } else {
            throw new SectionsRuntimeException("Must set an ajax upload url for fileupload");
          }
          info.addReadyStatements(Js.call_s(INIT, this, oe));
        }
      }
    }
  }
}
