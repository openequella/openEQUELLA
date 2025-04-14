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

package com.tle.web.sections.js.generic.expression;

import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.ElementId;

public class ScriptVariable extends AbstractExpression {
  protected final String name;
  private final ElementId elementId;

  public ScriptVariable(String name) {
    this(name, null);
  }

  public ScriptVariable(String name, ElementId id) {
    this.name = name;
    this.elementId = id;
  }

  @Override
  public String getExpression(RenderContext info) {
    if (elementId == null) {
      return name;
    }
    return name + elementId.getElementId(info);
  }

  @Override
  public void preRender(PreRenderContext info) {
    // nothing
  }
}
