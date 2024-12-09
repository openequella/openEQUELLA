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

package com.tle.web.sections.js.generic.statement;

import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.JSStatements;
import com.tle.web.sections.js.JSUtils;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.js.generic.expression.ScriptVariable;

public class DeclarationStatement implements JSStatements {
  protected final ScriptVariable var;
  protected final JSExpression defaultValue;

  public DeclarationStatement(ScriptVariable var, Object defaultValue) {
    this.var = var;
    this.defaultValue = JSUtils.convertExpression(defaultValue);
  }

  public DeclarationStatement(ScriptVariable var) {
    this(var, null);
  }

  @SuppressWarnings("nls")
  @Override
  public String getStatements(RenderContext info) {
    StringBuilder text = new StringBuilder("var ");
    text.append(var.getExpression(info));
    if (defaultValue != null) {
      text.append(" = ");
      text.append(defaultValue.getExpression(info));
    }
    text.append(";");
    text.append(Js.NEWLINE);
    return text.toString();
  }

  @Override
  public void preRender(PreRenderContext info) {
    SectionUtils.preRender(info, defaultValue);
  }
}
