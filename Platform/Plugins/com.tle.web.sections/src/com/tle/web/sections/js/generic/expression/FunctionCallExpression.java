package com.tle.web.sections.js.generic.expression;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.JSUtils;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;

@SuppressWarnings("nls")
@NonNullByDefault
public class FunctionCallExpression extends AbstractExpression
{
	protected JSCallable function;
	@Nullable
	protected JSExpression[] params;

	public FunctionCallExpression(JSCallable function, Object... params)
	{
		this.function = function;
		this.params = JSUtils.convertExpressions(params);
	}

	public FunctionCallExpression(String function, Object... params)
	{
		this(new ExternallyDefinedFunction(function), params);
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		info.preRender(function);
		info.preRender(params);
	}

	@Override
	public String getExpression(RenderContext info)
	{
		return function.getExpressionForCall(info, params);
	}

	@Override
	public String toString()
	{
		StringBuilder paramStr = new StringBuilder();
		if( params != null )
		{
			boolean first = true;
			for( JSExpression p : params )
			{
				if( !first )
				{
					paramStr.append(", ");
				}
				paramStr.append(p);
				first = false;
			}
		}
		return function + "(" + paramStr + ")";
	}
}
