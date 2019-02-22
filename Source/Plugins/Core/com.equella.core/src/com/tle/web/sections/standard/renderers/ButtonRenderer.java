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

package com.tle.web.sections.standard.renderers;

import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.jquery.JQuerySelector;
import com.tle.web.sections.jquery.JQuerySelector.Type;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.js.generic.function.DefaultDisableFunction;
import com.tle.web.sections.standard.js.JSDisableable;
import com.tle.web.sections.standard.model.HtmlComponentState;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

@SuppressWarnings("nls")
public class ButtonRenderer extends AbstractInputRenderer implements JSDisableable {
  public ButtonRenderer(HtmlComponentState state) {
    super(state, "button");
  }

  @Override
  protected void writeMiddle(SectionWriter writer) throws IOException {
    // nothing
  }

  @Override
  protected void writeEnd(SectionWriter writer) throws IOException {
    if (getHtmlState().isCancel()) {
      writer.writeTag(
          "input", "id", getResetId(), "type", "reset", "value", "RESET", "style", "display:none");
    }
  }

  @Override
  protected String getName(SectionInfo info) {
    return null;
  }

  @Override
  protected void prepareFirstAttributes(SectionWriter writer, Map<String, String> attrs)
      throws IOException {
    super.prepareFirstAttributes(writer, attrs);
    StringWriter swriter = new StringWriter();
    super.writeMiddle(new SectionWriter(swriter, writer));
    attrs.put("value", swriter.toString());
  }

  @Override
  protected void processHandler(
      SectionWriter writer, Map<String, String> attrs, String event, JSHandler handler) {
    if (!isStillAddClickHandler() && isDisabled() && JSHandler.EVENT_CLICK.equals(event)) {
      return;
    }
    if (getHtmlState().isCancel()) {
      super.processHandler(
          writer,
          attrs,
          event,
          Js.handler(
              Js.statement(
                  JQuerySelector.methodCallExpression(Type.ID, getResetId(), Js.function("click"))),
              handler.getStatements()));
      return;
    }
    super.processHandler(writer, attrs, event, handler);
  }

  private String getResetId() {
    return getId() + "_reset";
  }

  @Override
  protected boolean isStillAddClickHandler() {
    return true;
  }

  @Override
  public JSCallable createDisableFunction() {
    return new DefaultDisableFunction(this);
  }
}
