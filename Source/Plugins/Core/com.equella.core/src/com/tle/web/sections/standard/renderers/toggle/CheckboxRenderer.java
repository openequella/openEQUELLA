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

package com.tle.web.sections.standard.renderers.toggle;

import java.io.IOException;
import java.util.Map;

import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.generic.expression.ElementByIdExpression;
import com.tle.web.sections.js.generic.expression.PropertyExpression;
import com.tle.web.sections.js.generic.function.DefaultDisableFunction;
import com.tle.web.sections.js.generic.statement.AssignAsFunction;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TagRenderer;
import com.tle.web.sections.standard.js.JSDisableable;
import com.tle.web.sections.standard.js.JSValueComponent;
import com.tle.web.sections.standard.model.HtmlBooleanState;
import com.tle.web.sections.standard.renderers.AbstractInputRenderer;
import com.tle.web.sections.standard.renderers.LabelTagRenderer;

@SuppressWarnings("nls")
public class CheckboxRenderer extends AbstractInputRenderer
    implements JSValueComponent, JSDisableable {
  private final HtmlBooleanState bstate;

  public CheckboxRenderer(HtmlBooleanState state, String type) {
    super(state, type);
    this.bstate = state;
  }

  public CheckboxRenderer(HtmlBooleanState state) {
    this(state, "checkbox");
  }

  @Override
  protected void prepareFirstAttributes(SectionWriter writer, Map<String, String> attrs)
      throws IOException {
    super.prepareFirstAttributes(writer, attrs);
    if (bstate.isDisabled()) {
      attrs.put("disabled", "disabled");
    }
    if (bstate.getTitle() != null) {
      attrs.put("title", bstate.getTitle().getText());
    }
    attrs.put("value", bstate.getValue());
    attrs.put("checked", bstate.isChecked() ? "checked" : null);
  }

  @Override
  public SectionRenderable getNestedRenderable() {
    if (nestedRenderable == null) {
      if (state.getLabel() != null) {
        nestedRenderable = new LabelTagRenderer(this, null, state.createLabelRenderer());
      }
    }
    return nestedRenderable;
  }

  @Override
  public TagRenderer setNestedRenderable(SectionRenderable nested) {
    nestedRenderable = new LabelTagRenderer(this, "", nested);
    return this;
  }

  @Override
  protected void processHandler(
      SectionWriter writer, Map<String, String> attrs, String event, JSHandler handler) {
    if (event.equals(JSHandler.EVENT_CHANGE)) {
      super.processHandler(writer, attrs, JSHandler.EVENT_CLICK, handler);
    } else {
      super.processHandler(writer, attrs, event, handler);
    }
  }

  @Override
  public JSExpression createGetExpression() {
    return PropertyExpression.create(new ElementByIdExpression(this), "checked");
  }

  @Override
  public JSCallable createDisableFunction() {
    return new DefaultDisableFunction(this);
  }

  @Override
  public JSCallable createSetFunction() {
    return new AssignAsFunction(createGetExpression());
  }

  @Override
  public JSCallable createResetFunction() {
    return new AssignAsFunction(
        createGetExpression(),
        PropertyExpression.create(new ElementByIdExpression(this), "defaultChecked"));
  }
}
