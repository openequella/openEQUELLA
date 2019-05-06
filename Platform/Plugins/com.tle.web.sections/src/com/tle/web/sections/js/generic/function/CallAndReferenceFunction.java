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

package com.tle.web.sections.js.generic.function;

import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.js.ElementId;
import com.tle.web.sections.js.JSCallAndReference;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSFunction;
import com.tle.web.sections.js.generic.expression.ScriptVariable;
import com.tle.web.sections.js.generic.statement.AssignStatement;
import com.tle.web.sections.render.PreRenderable;

public final class CallAndReferenceFunction {
  @SuppressWarnings("nls")
  public static JSCallAndReference get(final JSFunction func, ElementId id) {
    if (func instanceof JSCallAndReference) {
      JSCallAndReference callRef = (JSCallAndReference) func;
      if (!id.isStaticId() || callRef.isStatic()) {
        return (JSCallAndReference) func;
      }
    }
    if (func instanceof JSCallable) {
      return new PassThroughFunction("ref", id, (JSCallable) func);
    }

    final ScriptVariable var = new ScriptVariable("fref", id); // $NON-NLS-1$
    ExternallyDefinedFunction extFunc =
        new ExternallyDefinedFunction(
            "fref",
            id,
            -1,
            new PreRenderable() {
              @Override
              public void preRender(PreRenderContext info) {
                info.addStatements(new AssignStatement(var, func));
              }
            });
    extFunc.setParamNumFunc(func);
    return extFunc;
  }

  private CallAndReferenceFunction() {
    throw new Error();
  }
}
