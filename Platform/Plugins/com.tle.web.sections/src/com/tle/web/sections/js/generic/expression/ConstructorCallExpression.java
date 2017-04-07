package com.tle.web.sections.js.generic.expression;

import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.JSCallable;

/**
 * @author aholland
 */
public class ConstructorCallExpression extends FunctionCallExpression
{
	public ConstructorCallExpression(JSCallable function, Object... params)
	{
		super(function, params);
	}

	@Override
	public String getExpression(RenderContext info)
	{
		return "new " + function.getExpressionForCall(info, params); //$NON-NLS-1$
	}
}
