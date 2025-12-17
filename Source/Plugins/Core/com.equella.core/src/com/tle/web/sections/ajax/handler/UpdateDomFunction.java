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

package com.tle.web.sections.ajax.handler;

import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.jquery.JQuerySelector;
import com.tle.web.sections.jquery.JQuerySelector.Type;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.generic.expression.FunctionCallExpression;
import com.tle.web.sections.js.generic.function.AnonymousFunction;
import com.tle.web.sections.js.generic.statement.FunctionCallStatement;
import java.util.Optional;

public class UpdateDomFunction implements JSCallable {
  private final AjaxFunction ajaxFunction;
  private final JSCallable effectFunction;
  private final JQuerySelector divQuery;
  private final JSCallable onSuccess;
  private final FunctionCallExpression afterPost;
  private final UpdateDomEvent domEvent;

  public UpdateDomFunction(
      UpdateDomEvent domEvent, String ajaxId, JSCallable effectFunction, JSCallable onSuccess) {
    this(domEvent, ajaxId, effectFunction, onSuccess, null);
  }

  public UpdateDomFunction(
      UpdateDomEvent domEvent,
      String ajaxId,
      JSCallable effectFunction,
      JSCallable onSuccess,
      String customEQ) {
    this.domEvent = domEvent;
    this.onSuccess = onSuccess;
    this.effectFunction = effectFunction;
    divQuery = new JQuerySelector(Type.ID, ajaxId);
    afterPost = new FunctionCallExpression(effectFunction, divQuery, null, onSuccess);

    String eventId = domEvent.getEventId();
    int paramCount = domEvent.getParameterCount();
    ajaxFunction =
        Optional.ofNullable(customEQ)
            .map(eq -> new AjaxFunction(eventId, paramCount, eq))
            .orElse(new AjaxFunction(eventId, paramCount));
  }

  @Override
  public int getNumberOfParams(RenderContext context) {
    return domEvent.getParameterCount();
  }

  @Override
  public String getExpressionForCall(RenderContext info, JSExpression... params) {
    JSExpression[] newParams = new JSExpression[params.length + 1];
    System.arraycopy(params, 0, newParams, 1, params.length);
    FunctionCallStatement domUpdateCall =
        new FunctionCallStatement(effectFunction, divQuery, AjaxGenerator.RESULTS_VAR, onSuccess);

    newParams[0] =
        new AnonymousFunction(domUpdateCall, AjaxGenerator.RESULTS_VAR, AjaxGenerator.STATUS_VAR);
    return ajaxFunction.getExpressionForCall(info, newParams)
        + ", " //$NON-NLS-1$
        + afterPost.getExpression(info);
  }

  @Override
  public void preRender(PreRenderContext info) {
    info.preRender(ajaxFunction, afterPost);
  }
}
