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

package com.tle.web.sections.jquery;

import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.jquery.JQuerySelector.Type;
import com.tle.web.sections.js.ElementId;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.JSStatements;
import com.tle.web.sections.js.generic.expression.PropertyExpression;
import com.tle.web.sections.js.generic.expression.ScriptExpression;

public class JQueryStatement implements JSStatements {
  private final JSExpression combined;

  public JQueryStatement(JQuerySelector selector, JSExpression property) {
    combined = PropertyExpression.create(selector, property);
  }

  public JQueryStatement(Type type, String id, String property) {
    this(new JQuerySelector(type, id), new ScriptExpression(property));
  }

  public JQueryStatement(Type type, String id, JSExpression property) {
    this(new JQuerySelector(type, id), property);
  }

  public JQueryStatement(ElementId id, String property) {
    this(new JQuerySelector(id), new ScriptExpression(property));
  }

  public JQueryStatement(ElementId id, JSExpression property) {
    this(new JQuerySelector(id), property);
  }

  @Override
  public String getStatements(RenderContext info) {
    return combined.getExpression(info) + ';';
  }

  @Override
  public void preRender(PreRenderContext info) {
    info.preRender(combined);
  }
}
