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

package com.tle.web.sections.standard.impl;

import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.jquery.libraries.JQueryCore;
import com.tle.web.sections.js.ElementId;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.expression.ElementByIdExpression;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.js.generic.statement.FunctionCallStatement;
import com.tle.web.sections.render.CssInclude;
import com.tle.web.sections.render.PreRenderable;

@SuppressWarnings("nls")
public class AutoSubmission implements PreRenderable {
  private static final PluginResourceHelper URL_HELPER =
      ResourcesService.getResourceHelper(AutoSubmission.class);

  protected static final String NAMESPACE = "AutoSubmit.";
  protected static final JSCallable SETUP =
      new ExternallyDefinedFunction(
          NAMESPACE + "setupAutoSubmit",
          new IncludeFile(URL_HELPER.url("js/autosubmit.js")),
          new CssInclude(URL_HELPER.url("css/autosubmit.css")));

  protected final ElementId control;
  protected final ElementId autoSubmitButton;

  public AutoSubmission(ElementId control, ElementId autoSubmitButton) {
    this.control = control;
    this.autoSubmitButton = autoSubmitButton;
  }

  @Override
  public void preRender(PreRenderContext info) {
    JQueryCore.appendReady(
        info,
        new FunctionCallStatement(
            SETUP,
            new ElementByIdExpression(control),
            new ElementByIdExpression(autoSubmitButton)));
  }
}
