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

import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.ElementId;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.function.RuntimeFunction;
import com.tle.web.sections.standard.js.JSMutableListComponent;

public class DelayedJSMutableListComponent extends DelayedJSListComponent<JSMutableListComponent> {
  private RuntimeFunction addFunction;
  private RuntimeFunction removeFunction;

  public DelayedJSMutableListComponent(ElementId id) {
    super(id);
  }

  public JSCallable createAddFunction() {
    if (addFunction == null) {
      addFunction =
          new DelayedFunction<JSMutableListComponent>(this, "add", id, 2) // $NON-NLS-1$
          {
            @Override
            protected JSCallable createRealFunction(
                RenderContext info, JSMutableListComponent renderer) {
              return renderer.createAddFunction();
            }
          };
    }
    return addFunction;
  }

  public JSCallable createRemoveFunction() {
    if (removeFunction == null) {
      removeFunction =
          new DelayedFunction<JSMutableListComponent>(this, "remove", id, 0) // $NON-NLS-1$
          {
            @Override
            protected JSCallable createRealFunction(
                RenderContext info, JSMutableListComponent renderer) {
              return renderer.createRemoveFunction();
            }
          };
    }
    return removeFunction;
  }
}
