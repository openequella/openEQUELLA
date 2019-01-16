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
import com.tle.web.sections.SectionsRuntimeException;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.JSCallAndReference;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.JSUtils;

@SuppressWarnings("nls")
@NonNullByDefault
public class ParentFrameFunction extends AbstractCallable {
  private final JSCallAndReference parentFunction;

  public ParentFrameFunction(JSCallAndReference parentFunction) {
    if (!parentFunction.isStatic()) {
      throw new SectionsRuntimeException(
          "You can only use static functions with parent frame functions");
    }
    this.parentFunction = parentFunction;
  }

  @Override
  protected String getCallExpression(RenderContext info, JSExpression[] params) {
    return JSUtils.createFunctionCall(
        info, "self.parent." + parentFunction.getExpression(info), params);
  }

  @Override
  public int getNumberOfParams(@Nullable RenderContext context) {
    return parentFunction.getNumberOfParams(context);
  }

  @Override
  public void preRender(PreRenderContext writer) {
    // nothing.. it happens on the parent
  }
}
