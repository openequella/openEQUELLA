package com.tle.web.sections.js.generic.function;

import com.tle.annotation.NonNullByDefault;
import com.tle.web.sections.SectionsRuntimeException;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSExpression;

@NonNullByDefault
public abstract class AbstractCallable implements JSCallable
{
	@SuppressWarnings("nls")
	@Override
	public String getExpressionForCall(RenderContext info, JSExpression... params)
	{
		int numParams = getNumberOfParams(info);
		if( numParams == -1 || numParams == params.length )
		{
			return getCallExpression(info, params);
		}
		throw new SectionsRuntimeException("Expected " + numParams + " parameters but found " + params.length);
	}

	protected abstract String getCallExpression(RenderContext info, JSExpression[] params);
}
