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

import java.io.IOException;
import java.util.List;

import com.google.common.collect.Lists;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.jquery.Jq;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.generic.AppendedElementId;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.render.CombinedRenderer;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.standard.model.HtmlComponentState;
import com.tle.web.sections.standard.model.HtmlListState;
import com.tle.web.sections.standard.model.HtmlValueState;
import com.tle.web.sections.standard.model.Option;
import com.tle.web.sections.standard.renderers.ButtonRenderer;

@SuppressWarnings("nls")
public class ButtonGroupRenderer extends AbstractFauxDropDownRenderer {
  public ButtonGroupRenderer(HtmlListState state) {
    super(state);
  }

  @Override
  protected void writeMiddle(SectionWriter writer) throws IOException {
    super.writeMiddle(writer);

    writer.writeTag(
        "div",
        "class",
        "btn-group",
        "data-toggle",
        multiple ? "buttons-checkbox" : "buttons-radio");

    final HtmlValueState ts = new HtmlValueState();
    ts.setElementId(new AppendedElementId(state, "fil"));
    ts.addClass("filterBox");

    List<SectionRenderable> filterButtons = Lists.newArrayList();

    for (Option<?> option : options) {
      HtmlComponentState buttonState = new HtmlComponentState();
      buttonState.addClass("btn btn-equella");
      buttonState.setLabel(new TextLabel(option.getName()));

      String value = option.getValue();
      if (value.equalsIgnoreCase(getSelectedValue())) {
        buttonState.addClass("active");
      }
      buttonState.setClickHandler(new OverrideHandler(clickFunc, value));

      ButtonRenderer buttonRenderer = new ButtonRenderer(buttonState);
      buttonRenderer.setAttribute("name", option.getName());
      filterButtons.add(buttonRenderer);
    }
    writer.render(CombinedRenderer.combineMultipleResults(filterButtons));

    JSExpression createToggle = Js.methodCall(Jq.$(this), Js.function("button"), "toggle");
    writer.addReadyStatements(Js.statement(createToggle));

    writer.endTag("div");
  }

  @Override
  protected String getTag() {
    return "div";
  }

  @Override
  public void preRender(PreRenderContext info) {
    info.preRender(Bootstrap.PRERENDER);
    super.preRender(info);
  }

  @Override
  public JSCallable createSetAllFunction() {
    throw new UnsupportedOperationException("Not yet");
  }
}
