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

package com.tle.web.sections.js.generic.function;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.JSAssignable;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.JSStatements;
import com.tle.web.sections.js.JSUtils;
import com.tle.web.sections.js.generic.expression.FunctionCallExpression;
import com.tle.web.sections.js.generic.statement.ReturnStatement;

@NonNullByDefault
public class AssignableFunction extends AbstractFunctionDefinition implements JSAssignable {
  private final JSCallable call;

  AssignableFunction(JSCallable call) {
    this.call = call;
  }

  public static JSAssignable get(JSCallable callable) {
    if (callable instanceof JSAssignable) {
      return (JSAssignable) callable;
    }
    return new AssignableFunction(callable);
  }

  @Override
  public int getNumberOfParams(@Nullable RenderContext context) {
    int numParams = call.getNumberOfParams(context);
    if (numParams == -1) {
      return 0;
    }
    return numParams;
  }

  @Nullable
  @Override
  protected String getFunctionName(@Nullable RenderContext context) {
    return null;
  }

  @Override
  protected JSStatements getBody(@Nullable RenderContext context) {
    if (body == null) {
      body = new ReturnStatement(new FunctionCallExpression(call, (Object[]) getParams(context)));
    }
    return body;
  }

  @Override
  protected JSExpression[] getParams(@Nullable RenderContext context) {
    if (params == null) {
      params = JSUtils.createParameters(getNumberOfParams(context));
    }
    return params;
  }

  @Override
  public String getExpression(@Nullable RenderContext info) {
    return getDefinition(info);
  }
}
