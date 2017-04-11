package com.tle.web.sections.js.generic.function;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.JSUtils;

@NonNullByDefault
public class PrependedParameterFunction extends AbstractCallable
{
	protected JSCallable func;
	@Nullable
	protected JSExpression[] preparams;

	public PrependedParameterFunction(JSCallable func)
	{
		this.func = func;
		this.preparams = null;
	}

	public PrependedParameterFunction(JSCallable func, Object... params)
	{
		this.func = func;
		this.preparams = JSUtils.convertExpressions(params);
	}

	@Override
	protected String getCallExpression(RenderContext info, JSExpression[] params)
	{
		JSExpression[] prepExpr = getPrependedExpressions(info);
		JSExpression[] newParams = new JSExpression[prepExpr.length + params.length];
		System.arraycopy(prepExpr, 0, newParams, 0, prepExpr.length);
		System.arraycopy(params, 0, newParams, prepExpr.length, params.length);
		return func.getExpressionForCall(info, newParams);
	}

	@Override
	public int getNumberOfParams(RenderContext context)
	{
		int other = func.getNumberOfParams(context);
		if( other == -1 )
		{
			return -1;
		}
		return other - preparams.length;
	}

	protected JSExpression[] getPrependedExpressions(SectionInfo info)
	{
		if( preparams == null )
		{
			throw new Error("You must override getPrependedExpressions()"); //$NON-NLS-1$
		}
		return preparams;
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		info.preRender(func);
		info.preRender(getPrependedExpressions(info));
	}

}
