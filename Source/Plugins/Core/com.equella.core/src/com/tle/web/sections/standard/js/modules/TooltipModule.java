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

package com.tle.web.sections.standard.js.modules;

import com.tle.core.javascript.JavascriptModule;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.jquery.JQuerySelector;
import com.tle.web.sections.jquery.JQueryStatement;
import com.tle.web.sections.jquery.libraries.JQueryCore;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSStatements;
import com.tle.web.sections.js.generic.expression.FunctionCallExpression;
import com.tle.web.sections.js.generic.expression.ObjectExpression;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.render.PreRenderable;

@SuppressWarnings("nls")
public class TooltipModule implements JavascriptModule {
  private static final long serialVersionUID = 1L;

  private static final PreRenderable INCLUDE =
      new IncludeFile(
          ResourcesService.getResourceHelper(TooltipModule.class).url("js/tooltip.js"),
          JQueryCore.PRERENDER);

  public static final JSCallable TOOL_TIP = new ExternallyDefinedFunction("tooltip", 1, INCLUDE);

  public static JSStatements getTooltipStatements(
      JQuerySelector hoverSelector,
      JQuerySelector tipSelector,
      int delay,
      boolean useMousePosition) {
    return new JQueryStatement(
        hoverSelector,
        new FunctionCallExpression(
            TOOL_TIP,
            new ObjectExpression(
                "tipelement", tipSelector, "delay", delay, "mousePosition", useMousePosition)));
  }

  public static JSStatements getTooltipStatements(
      JQuerySelector hoverSelector,
      JQuerySelector tipSelector,
      int delay,
      int xOffset,
      int yOffset,
      boolean useMousePosition) {
    return new JQueryStatement(
        hoverSelector,
        new FunctionCallExpression(
            TOOL_TIP,
            new ObjectExpression(
                "xOffset",
                xOffset,
                "yOffset",
                yOffset,
                "tipelement",
                tipSelector,
                "delay",
                delay,
                "mousePosition",
                useMousePosition)));
  }

  public static JSStatements getTooltipStatements(
      JQuerySelector hoverSelector, String hoverHtml, int delay, boolean useMousePosition) {
    return new JQueryStatement(
        hoverSelector,
        new FunctionCallExpression(
            TOOL_TIP,
            new ObjectExpression(
                "tipelement",
                null,
                "delay",
                delay,
                "tiphtml",
                hoverHtml,
                "mousePosition",
                useMousePosition)));
  }

  @Override
  public String getDisplayName() {
    return "Tooltip";
  }

  @Override
  public String getId() {
    return "tooltip";
  }

  @Override
  public Object getPreRenderer() {
    return INCLUDE;
  }
}
