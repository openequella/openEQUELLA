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

package com.tle.web.sections.standard.renderers;

import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.generic.expression.CombinedExpression;
import com.tle.web.sections.js.generic.expression.PropertyExpression;
import com.tle.web.sections.js.generic.function.SimpleFunction;
import com.tle.web.sections.js.generic.statement.FunctionCallStatement;

public class SelectOptionsCallback extends SimpleFunction {
  private static PluginResourceHelper urlHelper =
      ResourcesService.getResourceHelper(SelectOptionsCallback.class);

  public SelectOptionsCallback(String id, JSExpression element) {
    super(
        "changeOptions" + id,
        new FunctionCallStatement(
            "changeSelectOptions",
            element, //$NON-NLS-1$ //$NON-NLS-2$
            new CombinedExpression(
                AjaxGenerator.RESULTS_VAR, new PropertyExpression("result"))), // $NON-NLS-1$
        AjaxGenerator.RESULTS_VAR,
        AjaxGenerator.STATUS_VAR);
  }

  @Override
  public void preRender(PreRenderContext info) {
    info.addJs(urlHelper.url("js/select.js")); // $NON-NLS-1$
    super.preRender(info);
  }
}
