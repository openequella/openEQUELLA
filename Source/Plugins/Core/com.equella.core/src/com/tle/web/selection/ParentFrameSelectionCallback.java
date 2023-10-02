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

package com.tle.web.selection;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.ParentFrameCallback;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.JSCallAndReference;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSStatements;
import com.tle.web.sections.js.generic.statement.FunctionCallStatement;
import com.tle.web.sections.render.PreRenderable;
import com.tle.web.sections.result.util.CloseWindowResult;
import java.io.IOException;

public class ParentFrameSelectionCallback extends ParentFrameCallback
    implements SelectionsMadeCallback {
  private static final long serialVersionUID = 1L;

  private final boolean closeWindow;

  public ParentFrameSelectionCallback(JSCallAndReference function, boolean closeWindow) {
    super(function);
    this.closeWindow = closeWindow;
  }

  @Override
  public boolean executeSelectionsMade(SectionInfo info, SelectionSession session) {
    final JSCallable call = createParentFrameCall();
    final RenderContext renderContext = info.getRootRenderContext();

    final JSStatements callStatements;
    try {
      callStatements =
          new FunctionCallStatement(
              call, new ObjectMapper().writeValueAsString(session.getSelectionDetails()));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    if (closeWindow) {
      renderContext.setRenderedBody(new CloseWindowResult(callStatements));
    } else {
      renderContext.setRenderedBody(
          (PreRenderable) info1 -> info1.addReadyStatements(callStatements));
    }

    return false;
  }
}
