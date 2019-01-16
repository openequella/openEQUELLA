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

package com.tle.web.sections.standard.renderers.codemirror;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.tle.common.i18n.CurrentLocale;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.jquery.JQuerySelector;
import com.tle.web.sections.js.ElementId;
import com.tle.web.sections.js.JSCallAndReference;
import com.tle.web.sections.js.generic.AppendedElementId;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.generic.StatementHandler;
import com.tle.web.sections.js.generic.expression.ScriptVariable;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.render.LabelRenderer;
import com.tle.web.sections.render.ListTagRenderer;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TagRenderer;
import com.tle.web.sections.render.TagState;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.standard.js.modules.CodeMirrorLibrary;
import com.tle.web.sections.standard.js.modules.TooltipModule;
import com.tle.web.sections.standard.model.CodeMirrorState;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.renderers.AbstractElementRenderer;
import com.tle.web.sections.standard.renderers.DivRenderer;
import com.tle.web.sections.standard.renderers.LinkRenderer;
import com.tle.web.sections.standard.renderers.TextAreaRenderer;

public class CodeMirrorRenderer extends AbstractElementRenderer {
  private static final PluginResourceHelper urlHelper =
      ResourcesService.getResourceHelper(CodeMirrorRenderer.class);
  private final IncludeFile codeMirrorHelper =
      new IncludeFile(urlHelper.url("js/codemirror/cmhelper.js"));
  private final String helpLinkName = urlHelper.key("codemirror.shortcuts.link.name");
  private final String helpText = urlHelper.key("codemirror.shortcuts");
  private final String helpTextFullscreen = urlHelper.key("codemirror.shortcuts.fullscreen");

  private CodeMirrorState codeMirrorState;

  public CodeMirrorRenderer(CodeMirrorState state) {
    super(state);
    codeMirrorState = state;
  }

  @Override
  protected void writeStart(SectionWriter writer, Map<String, String> attrs) throws IOException {
    writer.write("<div class='editor-container'>");
  }

  @Override
  protected void writeMiddle(SectionWriter writer) throws IOException {
    // render codemirror
    ScriptVariable editorVariable = getEditor(codeMirrorState);
    final JSCallAndReference codeRefresh =
        new ExternallyDefinedFunction("refresh", codeMirrorHelper);
    final JSCallAndReference codeSave = new ExternallyDefinedFunction("save", codeMirrorHelper);
    codeMirrorState.addReadyStatements(codeRefresh, editorVariable);
    codeMirrorState.setEventHandler(
        JSHandler.EVENT_PRESUBMIT, new StatementHandler(codeSave, editorVariable));
    TextAreaRenderer textAreaRenderer = new TextAreaRenderer(codeMirrorState);
    writer.render(textAreaRenderer);

    writer.write("</div>");
  }

  @Override
  protected void writeEnd(SectionWriter writer) throws IOException {
    super.writeEnd(writer);
    if (codeMirrorState.isShowHelp()) {
      ElementId linkDivId = new AppendedElementId(this, "linkDiv");
      ElementId textDivId = new AppendedElementId(this, "textDiv");

      // render help hover link
      HtmlLinkState link = new HtmlLinkState();
      link.setElementId(linkDivId);
      link.addClass("help-text-link");
      link.setLabel(new TextLabel(CurrentLocale.get(helpLinkName)));
      link.addReadyStatements(
          TooltipModule.getTooltipStatements(
              new JQuerySelector(linkDivId), new JQuerySelector(textDivId), 300, 130, -135, false));
      link.setClickHandler(new OverrideHandler());
      LinkRenderer linkRenderer = new LinkRenderer(link);
      linkRenderer.setTitle(null);
      writer.render(linkRenderer);

      // render help text div
      DivRenderer div = new DivRenderer(new TagState(textDivId));
      div.addClass("help-text-div tooltip");
      ListTagRenderer list = new ListTagRenderer(getShortcutsAsRenderable());
      div.setNestedRenderable(list);
      writer.render(div);
    }
  }

  private List<SectionRenderable> getShortcutsAsRenderable() {
    String shortcuts = CurrentLocale.get(helpText);
    if (codeMirrorState.isAllowFullScreen()) {
      String fullscreen = CurrentLocale.get(helpTextFullscreen);
      shortcuts += fullscreen;
    }

    List<SectionRenderable> renderables = new ArrayList<>();
    for (String shortcut : shortcuts.split(",")) {
      TagRenderer li = new TagRenderer("li", new TagState());
      li.setNestedRenderable(new LabelRenderer(new TextLabel(shortcut)));
      renderables.add(li);
    }
    return renderables;
  }

  private ScriptVariable getEditor(CodeMirrorState state) {
    ScriptVariable editorVariable = null;
    switch (state.getEditorType()) {
      case JAVASCRIPT_EDITOR:
        editorVariable = CodeMirrorLibrary.addJavascriptEditing(state, state.isAllowFullScreen());
        break;
      case FREEMARKER_EDITOR:
        editorVariable = CodeMirrorLibrary.addFreemarkerEditing(state, state.isAllowFullScreen());
        break;
      case CSS_EDITOR:
        editorVariable = CodeMirrorLibrary.addCssEditing(state, state.isAllowFullScreen());
        break;
      default:
        editorVariable = CodeMirrorLibrary.addJavascriptEditing(state, true);
        break;
    }

    return editorVariable;
  }

  @Override
  protected String getTag() {
    return "textarea";
  }
}
