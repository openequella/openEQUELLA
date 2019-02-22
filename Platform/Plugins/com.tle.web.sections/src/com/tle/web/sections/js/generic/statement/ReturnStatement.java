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

package com.tle.web.sections.js.generic.statement;

import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.JSStatements;
import com.tle.web.sections.js.JSUtils;

public class ReturnStatement implements JSStatements {
  private final JSExpression expression;

  public ReturnStatement(Object expr) {
    this.expression = JSUtils.convertExpression(expr);
  }

  public ReturnStatement(JSExpression expr) {
    this.expression = expr;
    if (expr == null) {
      throw new NullPointerException();
    }
  }

  @Override
  public String getStatements(RenderContext info) {
    return "return " + expression.getExpression(info) + ";"; // $NON-NLS-1$ //$NON-NLS-2$
  }

  @Override
  public void preRender(PreRenderContext info) {
    info.preRender(expression);
  }
}
