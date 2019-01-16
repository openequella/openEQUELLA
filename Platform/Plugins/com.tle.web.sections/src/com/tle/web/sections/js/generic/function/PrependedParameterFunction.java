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
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.JSUtils;

@NonNullByDefault
public class PrependedParameterFunction extends AbstractCallable {
  protected JSCallable func;
  @Nullable protected JSExpression[] preparams;

  public PrependedParameterFunction(JSCallable func) {
    this.func = func;
    this.preparams = null;
  }

  public PrependedParameterFunction(JSCallable func, Object... params) {
    this.func = func;
    this.preparams = JSUtils.convertExpressions(params);
  }

  @Override
  protected String getCallExpression(RenderContext info, JSExpression[] params) {
    JSExpression[] prepExpr = getPrependedExpressions(info);
    JSExpression[] newParams = new JSExpression[prepExpr.length + params.length];
    System.arraycopy(prepExpr, 0, newParams, 0, prepExpr.length);
    System.arraycopy(params, 0, newParams, prepExpr.length, params.length);
    return func.getExpressionForCall(info, newParams);
  }

  @Override
  public int getNumberOfParams(RenderContext context) {
    int other = func.getNumberOfParams(context);
    if (other == -1) {
      return -1;
    }
    return other - preparams.length;
  }

  protected JSExpression[] getPrependedExpressions(SectionInfo info) {
    if (preparams == null) {
      throw new Error("You must override getPrependedExpressions()"); // $NON-NLS-1$
    }
    return preparams;
  }

  @Override
  public void preRender(PreRenderContext info) {
    info.preRender(func);
    info.preRender(getPrependedExpressions(info));
  }
}
