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

package com.tle.web.sections.equella.render;

import com.tle.common.Check;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.equella.annotation.PluginResourceHandler;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.jquery.JQuerySelector;
import com.tle.web.sections.jquery.libraries.JQueryStylishSelect;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.js.generic.expression.ObjectExpression;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.js.generic.function.PrependedParameterFunction;
import com.tle.web.sections.standard.model.HtmlComponentState;
import com.tle.web.sections.standard.model.HtmlListState;
import com.tle.web.sections.standard.renderers.list.DropDownRenderer;
import java.util.Set;

@SuppressWarnings("nls")
public class StylishDropDownRenderer extends DropDownRenderer {
  static {
    PluginResourceHandler.init(StylishDropDownRenderer.class);
  }

  /** integer pixel heights only */
  public static final String KEY_MAX_HEIGHT = "StylishMaxHeight";

  /** boolean */
  public static final String KEY_ALWAYS_DISPLAY_UP = "StylishAlwaysDisplayUp";

  // these are attributes passed into the stylishselect javascript
  private static final String KEY_JS_MAX_HEIGHT = "ddMaxHeight";
  private static final String KEY_JS_CLASSES = "containerClass";
  private static final String KEY_JS_ALWAYS_DISPLAY_UP = "alwaysDisplayUp";

  private static final IncludeFile STYLISH_HELPER_LIB =
      new IncludeFile(
          ResourcesService.getResourceHelper(StylishDropDownRenderer.class)
              .url("scripts/component/stylishhelper.js"));
  private static final JSCallable SETTER =
      new ExternallyDefinedFunction("setValue", 2, STYLISH_HELPER_LIB);
  private static final JSCallable GETTER =
      new ExternallyDefinedFunction("getValue", 1, STYLISH_HELPER_LIB);

  private static final JSCallable RESET =
      new ExternallyDefinedFunction("reset", 1, STYLISH_HELPER_LIB);

  public StylishDropDownRenderer(HtmlListState state) {
    super(state);
  }

  @Override
  public void preRender(PreRenderContext info) {
    final ObjectExpression params = new ObjectExpression();
    final HtmlComponentState selectState = getHtmlState();

    Integer maxHeight = selectState.getAttribute(KEY_MAX_HEIGHT);
    if (maxHeight == null) {
      maxHeight = 300;
    }
    params.put(KEY_JS_MAX_HEIGHT, maxHeight);

    // inherit any classes put on the SELECT element
    final Set<String> classes = selectState.getStyleClasses();
    final StringBuilder classString = new StringBuilder();
    boolean first = true;
    if (!Check.isEmpty(classes)) {
      for (String clas : classes) {
        if (!first) {
          classString.append(' ');
        }
        classString.append(clas);
        first = false;
      }
    }
    params.put(KEY_JS_CLASSES, classString.toString());

    Boolean alwaysDisplayUp = selectState.getAttribute(KEY_ALWAYS_DISPLAY_UP);
    if (alwaysDisplayUp != null && alwaysDisplayUp.booleanValue()) {
      params.put(KEY_JS_ALWAYS_DISPLAY_UP, true);
    }

    info.addReadyStatements(
        JQueryStylishSelect.setupStylishSelect(new JQuerySelector(this), params));
    super.preRender(info);
  }

  @Override
  public JSExpression createGetExpression() {
    return Js.call(GETTER, this);
  }

  @Override
  public JSCallable createSetFunction() {
    return new PrependedParameterFunction(SETTER, this);
  }

  @Override
  public JSCallable createResetFunction() {
    return new PrependedParameterFunction(RESET, this);
  }

  // TODO: other JS methods
}
