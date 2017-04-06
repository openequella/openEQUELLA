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

public class UpdateDomFunction implements JSCallable
{
	private final AjaxFunction ajaxFunction;
	private final JSCallable effectFunction;
	private final JQuerySelector divQuery;
	private final JSCallable onSuccess;
	private final FunctionCallExpression afterPost;
	private final UpdateDomEvent domEvent;

	public UpdateDomFunction(UpdateDomEvent domEvent, String ajaxId, JSCallable effectFunction, JSCallable onSuccess)
	{
		this.domEvent = domEvent;
		this.onSuccess = onSuccess;
		this.effectFunction = effectFunction;
		divQuery = new JQuerySelector(Type.ID, ajaxId);
		afterPost = new FunctionCallExpression(effectFunction, divQuery, null, onSuccess);
		ajaxFunction = new AjaxFunction(domEvent.getEventId(), domEvent.getParameterCount());
	}

	@Override
	public int getNumberOfParams(RenderContext context)
	{
		return domEvent.getParameterCount();
	}

	@Override
	public String getExpressionForCall(RenderContext info, JSExpression... params)
	{
		JSExpression[] newParams = new JSExpression[params.length + 1];
		System.arraycopy(params, 0, newParams, 1, params.length);
		FunctionCallStatement domUpdateCall = new FunctionCallStatement(effectFunction, divQuery,
			AjaxGenerator.RESULTS_VAR, onSuccess);

		newParams[0] = new AnonymousFunction(domUpdateCall, AjaxGenerator.RESULTS_VAR, AjaxGenerator.STATUS_VAR);
		return ajaxFunction.getExpressionForCall(info, newParams) + ", " //$NON-NLS-1$
			+ afterPost.getExpression(info);
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		info.preRender(ajaxFunction, afterPost);
	}
}
