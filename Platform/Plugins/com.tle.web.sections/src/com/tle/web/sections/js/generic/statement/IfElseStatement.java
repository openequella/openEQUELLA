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
import com.tle.web.sections.js.JSStatements;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.js.generic.expression.BooleanExpression;

public class IfElseStatement extends IfStatement {
  private final JSStatements elseBody;

  public IfElseStatement(BooleanExpression condition, JSStatements body, JSStatements elseBody) {
    super(condition, body);
    this.elseBody = elseBody;
  }

  @SuppressWarnings("nls")
  @Override
  public String getStatements(RenderContext info) {
    StringBuilder text = new StringBuilder(super.getStatements(info));
    text.append("else")
        .append(Js.NEWLINE)
        .append("{")
        .append(Js.NEWLINE)
        .append(elseBody.getStatements(info))
        .append(Js.NEWLINE)
        .append("}")
        .append(Js.NEWLINE);
    return text.toString();
  }

  @Override
  public void preRender(PreRenderContext info) {
    SectionUtils.preRender(info, body, condition);
  }
}
