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

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.ElementId;
import com.tle.web.sections.js.JSCallAndReference;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.JSStatements;
import com.tle.web.sections.js.JSUtils;

@NonNullByDefault
public class DynamicNamedFunction extends AbstractCallable
    implements JSCallAndReference, FunctionDefinition {
  private final String name;
  @Nullable private final ElementId elementId;
  private final JSExpression[] parameters;

  public DynamicNamedFunction(String functionName, @Nullable ElementId id, JSExpression... params) {
    this.name = functionName;
    this.elementId = id;
    this.parameters = params;
  }

  // elementId cannot possibly be null after check
  @SuppressWarnings("null")
  private String getFullName(@Nullable SectionInfo info) {
    if (elementId == null) {
      return name;
    }
    return name + elementId.getElementId(info);
  }

  @Override
  public boolean isStatic() {
    return elementId == null || elementId.isStaticId();
  }

  @Override
  public int getNumberOfParams(@Nullable RenderContext context) {
    return parameters.length;
  }

  public void setStatements(SectionInfo info, JSStatements statements) {
    info.setAttribute(this, statements);
  }

  public boolean isBodySet(SectionInfo info) {
    return info.getAttribute(this) != null;
  }

  @Override
  protected String getCallExpression(RenderContext info, JSExpression[] params) {
    return JSUtils.createFunctionCall(info, getFullName(info), params);
  }

  @Override
  public void preRender(final PreRenderContext info) {
    info.addStatements(new FunctionDefinitionStatement(this));
  }

  @Override
  public String getExpression(@Nullable RenderContext info) {
    return getFullName(info);
  }

  @Override
  public JSStatements createFunctionBody(@Nullable RenderContext context, JSExpression[] params) {
    if (context == null) {
      throw new Error("Need a context to retrieve the function body");
    }
    final JSStatements statements = context.getAttribute(this);
    if (statements == null) {
      throw new Error("Function body was not set");
    }
    return statements;
  }

  @Override
  public String getFunctionName(@Nullable RenderContext context) {
    return getFullName(context);
  }

  @Override
  public JSExpression[] getFunctionParams(@Nullable RenderContext context) {
    return parameters;
  }
}
