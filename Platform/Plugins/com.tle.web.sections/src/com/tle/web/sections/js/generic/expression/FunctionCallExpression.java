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

package com.tle.web.sections.js.generic.expression;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.JSUtils;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;

@SuppressWarnings("nls")
@NonNullByDefault
public class FunctionCallExpression extends AbstractExpression {
  protected JSCallable function;
  @Nullable protected JSExpression[] params;

  public FunctionCallExpression(JSCallable function, Object... params) {
    this.function = function;
    this.params = JSUtils.convertExpressions(params);
  }

  public FunctionCallExpression(String function, Object... params) {
    this(new ExternallyDefinedFunction(function), params);
  }

  @Override
  public void preRender(PreRenderContext info) {
    info.preRender(function);
    info.preRender(params);
  }

  @Override
  public String getExpression(RenderContext info) {
    return function.getExpressionForCall(info, params);
  }

  @Override
  public String toString() {
    StringBuilder paramStr = new StringBuilder();
    if (params != null) {
      boolean first = true;
      for (JSExpression p : params) {
        if (!first) {
          paramStr.append(", ");
        }
        paramStr.append(p);
        first = false;
      }
    }
    return function + "(" + paramStr + ")";
  }
}
