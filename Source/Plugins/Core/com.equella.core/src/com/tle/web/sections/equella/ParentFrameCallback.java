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

package com.tle.web.sections.equella;

import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionsRuntimeException;
import com.tle.web.sections.js.JSCallAndReference;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.ParentFrameFunction;
import com.tle.web.sections.js.generic.statement.FunctionCallStatement;
import com.tle.web.sections.result.util.CloseWindowResult;

public class ParentFrameCallback implements ModalSessionCallback {
  private static final long serialVersionUID = 1L;

  protected final String parentFunction;

  public ParentFrameCallback(JSCallAndReference function) {
    if (!function.isStatic()) {
      throw new SectionsRuntimeException("Callback function must be static"); // $NON-NLS-1$
    }
    this.parentFunction = function.getExpression(null);
  }

  public JSCallable createParentFrameCall() {
    return new ParentFrameFunction(new ExternallyDefinedFunction(parentFunction));
  }

  @Override
  public void executeModalFinished(SectionInfo info, ModalSession session) {
    JSCallable call = createParentFrameCall();
    info.getRootRenderContext()
        .setRenderedBody(new CloseWindowResult(new FunctionCallStatement(call)));
  }
}
