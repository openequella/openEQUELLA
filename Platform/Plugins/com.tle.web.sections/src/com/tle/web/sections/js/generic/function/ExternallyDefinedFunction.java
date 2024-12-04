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
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.ElementId;
import com.tle.web.sections.js.JSCallAndReference;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.JSFunction;
import com.tle.web.sections.js.JSUtils;
import com.tle.web.sections.js.generic.expression.PropertyExpression;
import com.tle.web.sections.render.PreRenderable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@NonNullByDefault
public class ExternallyDefinedFunction implements JSCallAndReference {
  @Nullable private final String name;
  @Nullable private final ElementId elemId;
  @Nullable private JSFunction paramNumFunc;
  private final int numParams;
  @Nullable private final JSExpression callExpr;
  private final List<PreRenderable> preRenderers = new ArrayList<PreRenderable>();
  private boolean staticName;

  public ExternallyDefinedFunction(String name) {
    this(name, -1);
  }

  public ExternallyDefinedFunction(
      JSCallAndReference clazz, String method, int numParams, PreRenderable... preRenderers) {
    this(PropertyExpression.create(clazz, method), numParams, preRenderers);
    this.staticName = clazz.isStatic();
  }

  @Override
  public boolean isStatic() {
    return staticName;
  }

  public ExternallyDefinedFunction(String name, int numParams, PreRenderable... preRenderers) {
    this(name, null, numParams, preRenderers);
  }

  public ExternallyDefinedFunction(
      String name, @Nullable ElementId elemId, int numParams, PreRenderable... preRenderers) {
    this.callExpr = null;
    this.name = name;
    this.elemId = elemId;
    this.numParams = numParams;
    this.preRenderers.addAll(Arrays.asList(preRenderers));
    this.staticName = elemId == null || elemId.isStaticId();
  }

  public ExternallyDefinedFunction(
      JSExpression callExpr, int numParams, PreRenderable... preRenderers) {
    this.callExpr = callExpr;
    this.elemId = null;
    this.name = null;
    this.numParams = numParams;
    this.preRenderers.addAll(Arrays.asList(preRenderers));
    this.staticName = false;
  }

  @Override
  public int getNumberOfParams(@Nullable RenderContext context) {
    return paramNumFunc != null ? paramNumFunc.getNumberOfParams(context) : numParams;
  }

  public ExternallyDefinedFunction(String name, PreRenderable... preRenderers) {
    this(name, -1, preRenderers);
  }

  public ExternallyDefinedFunction addPreRenderer(PreRenderable preRenderable) {
    preRenderers.add(preRenderable);
    return this;
  }

  @Override
  public String getExpression(RenderContext info) {
    return getFunctionName(info);
  }

  @Override
  public String getExpressionForCall(RenderContext info, JSExpression... params) {
    return JSUtils.createFunctionCall(info, getFunctionName(info), params);
  }

  private String getFunctionName(RenderContext info) {
    if (callExpr != null) {
      return callExpr.getExpression(info);
    }
    if (elemId != null) {
      return name + elemId.getElementId(info);
    }
    return name;
  }

  @Override
  public void preRender(PreRenderContext info) {
    info.preRender(callExpr);
    info.preRender(preRenderers);
  }

  public static Map<String, JSFunction> createFunctions(String jsFile, String... funcs) {
    IncludeFile include = new IncludeFile(jsFile);
    Map<String, JSFunction> funcMap = new HashMap<String, JSFunction>();
    for (String func : funcs) {
      JSFunction exFunc = new ExternallyDefinedFunction(func, include);
      funcMap.put(func, exFunc);
    }
    return funcMap;
  }

  public void setParamNumFunc(JSFunction paramNumFunc) {
    this.paramNumFunc = paramNumFunc;
  }

  @Override
  public String toString() {
    return name;
  }
}
