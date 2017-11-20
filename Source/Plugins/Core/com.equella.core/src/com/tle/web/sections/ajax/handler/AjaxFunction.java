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

package com.tle.web.sections.ajax.handler;

import java.util.Arrays;

import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.JSUtils;
import com.tle.web.sections.js.generic.expression.ArrayExpression;
import com.tle.web.sections.js.generic.expression.CurrentForm;

public class AjaxFunction implements JSCallable
{
	private final String ajaxMethod;
	private final int numParams;

	public AjaxFunction(String ajaxMethod, int numParams)
	{
		this.ajaxMethod = ajaxMethod;
		this.numParams = numParams;
	}

	@Override
	public int getNumberOfParams(RenderContext context)
	{
		return 1 + numParams;
	}

	@Override
	public String getExpressionForCall(RenderContext info, JSExpression... params)
	{
		JSExpression[] newparams = JSUtils.convertExpressions(CurrentForm.EXPR, ajaxMethod, new ArrayExpression(
			(Object[]) Arrays.copyOfRange(params, 1, params.length)), params[0]);
		return JSUtils.createFunctionCall(info, "postAjaxJSON", newparams); //$NON-NLS-1$
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		info.preRender(AjaxGenerator.AJAX_LIBRARY, CurrentForm.EXPR);
	}
}
