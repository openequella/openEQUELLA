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
