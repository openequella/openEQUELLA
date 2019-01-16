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

package com.tle.web.sections.standard.js.impl;

import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.ElementId;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.generic.expression.RuntimeExpression;
import com.tle.web.sections.js.generic.function.RuntimeFunction;
import com.tle.web.sections.standard.js.JSValueComponent;

public class DelayedJSValueComponent<T extends JSValueComponent> extends AbstractDelayedJS<T>
    implements JSValueComponent {
  private RuntimeFunction setFunction;
  private RuntimeExpression getExpression;
  private RuntimeFunction resetFunction;

  public DelayedJSValueComponent(ElementId id) {
    super(id);
  }

  @Override
  public JSCallable createSetFunction() {
    if (setFunction == null) {
      setFunction =
          new DelayedFunction<T>(this, "set", id, 1) // $NON-NLS-1$
          {
            @Override
            protected JSCallable createRealFunction(RenderContext info, T renderer) {
              return renderer.createSetFunction();
            }
          };
    }
    return setFunction;
  }

  @Override
  public JSExpression createGetExpression() {
    if (getExpression == null) {
      getExpression =
          new DelayedExpression<T>(this, "get", id) // $NON-NLS-1$
          {
            @Override
            protected JSExpression createRealExpression(SectionInfo info, T renderer) {
              return renderer.createGetExpression();
            }
          };
    }
    return getExpression;
  }

  @Override
  public JSCallable createResetFunction() {
    if (resetFunction == null) {
      resetFunction =
          new DelayedFunction<T>(this, "reset", id, 0) // $NON-NLS-1$
          {
            @Override
            protected JSCallable createRealFunction(RenderContext info, T renderer) {
              return renderer.createResetFunction();
            }
          };
    }
    return resetFunction;
  }
}
