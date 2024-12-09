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

package com.tle.web.sections.equella.dialog;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.common.Check;
import com.tle.core.guice.Bind;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.jquery.Jq;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.dialog.model.DialogModel;

@SuppressWarnings("nls")
@Bind
@NonNullByDefault
public class Prompt extends AbstractOkayableDialog<DialogModel> {
  private static final PluginResourceHelper resources =
      ResourcesService.getResourceHelper(Prompt.class);

  private static final IncludeFile INCLUDE =
      new IncludeFile(resources.url("scripts/utils/promptdialog.js"));
  private static final JSCallable GET_TEXT = new ExternallyDefinedFunction("getText", INCLUDE);
  private static final JSCallable TEXT_CHANGE =
      new ExternallyDefinedFunction("textChange", INCLUDE);

  @Component(name = "t", stateful = false)
  private TextField text;

  private Label prompt;
  private Label defaultText;

  @ViewFactory private FreemarkerFactory view;

  public Prompt() {
    setAjax(true);
  }

  @Nullable
  @Override
  protected Label getTitleLabel(RenderContext context) {
    return null;
  }

  @Override
  protected SectionRenderable getRenderableContents(RenderContext context) {
    if (defaultText != null && Check.isEmpty(text.getValue(context))) {
      text.setValue(context, defaultText.getText());
    }
    return view.createResult("utils/promptdialog.ftl", this);
  }

  @Override
  public String getWidth() {
    return "500px";
  }

  @Override
  public String getHeight() {
    return "220px";
  }

  @Override
  public DialogModel instantiateDialogModel(@Nullable SectionInfo info) {
    return new DialogModel();
  }

  @Override
  public void treeFinished(String id, SectionTree tree) {
    super.treeFinished(id, tree);
    text.addEventStatements("change", Js.call_s(TEXT_CHANGE, Jq.$(text), Jq.$(getOk())));
    if (prompt == null) {
      throw new RuntimeException("Label not set on Prompt dialog");
    }
  }

  public TextField getText() {
    return text;
  }

  public JSExpression getTextExpression() {
    return Js.call(GET_TEXT, Jq.$(text));
  }

  public Label getPrompt() {
    return prompt;
  }

  public void setPrompt(Label prompt) {
    this.prompt = prompt;
  }

  public Label getDefaultText() {
    return defaultText;
  }

  public void setDefaultText(Label defaultText) {
    this.defaultText = defaultText;
  }
}
