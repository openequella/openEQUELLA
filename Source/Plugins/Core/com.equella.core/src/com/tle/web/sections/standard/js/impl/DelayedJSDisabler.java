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

package com.tle.web.sections.standard.js.impl;

import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.ElementId;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.function.RuntimeFunction;
import com.tle.web.sections.standard.js.JSDisableable;
import com.tle.web.sections.standard.model.HtmlComponentState;

public class DelayedJSDisabler extends AbstractDelayedJS<JSDisableable> implements JSDisableable {
  private RuntimeFunction disabler;

  public DelayedJSDisabler(final ElementId id) {
    super(id);
    id.registerUse();
  }

  public DelayedJSDisabler(HtmlComponentState state) {
    this((ElementId) state);
    state.addRendererCallback(this);
  }

  @Override
  public JSCallable createDisableFunction() {
    if (disabler == null) {
      disabler =
          new DelayedFunction<JSDisableable>(this, "ddis", id, 1) // $NON-NLS-1$
          {
            @Override
            protected JSCallable createRealFunction(RenderContext info, JSDisableable renderer) {
              return renderer.createDisableFunction();
            }
          };
    }
    return disabler;
  }
}
