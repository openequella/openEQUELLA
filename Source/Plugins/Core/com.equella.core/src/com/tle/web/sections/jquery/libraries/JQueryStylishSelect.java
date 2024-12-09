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

package com.tle.web.sections.jquery.libraries;

import com.tle.common.i18n.CurrentLocale;
import com.tle.core.javascript.JavascriptModule;
import com.tle.web.sections.jquery.JQueryLibraryInclude;
import com.tle.web.sections.jquery.JQuerySelector;
import com.tle.web.sections.jquery.JQueryStatement;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSStatements;
import com.tle.web.sections.js.generic.expression.FunctionCallExpression;
import com.tle.web.sections.js.generic.expression.ObjectExpression;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.render.PreRenderable;

public class JQueryStylishSelect implements JavascriptModule {
  private static final long serialVersionUID = 1L;

  public static final PreRenderable PRERENDER =
      new JQueryLibraryInclude(
              "jquery.stylish-select.js",
              JQueryLibraryInclude.cssb("jquery.stylish-select.css").hasMin().make())
          .hasMin();

  private static final JSCallable SETUP_STYLISH =
      new ExternallyDefinedFunction("sSelect", PRERENDER);

  public static JSStatements setupStylishSelect(JQuerySelector selector, ObjectExpression params) {
    return new JQueryStatement(selector, new FunctionCallExpression(SETUP_STYLISH, params));
  }

  @Override
  public String getDisplayName() {
    return CurrentLocale.get("com.tle.web.sections.jquery.modules.stylishselect.name");
  }

  @Override
  public String getId() {
    return "stylish-select";
  }

  @Override
  public Object getPreRenderer() {
    return PRERENDER;
  }
}
