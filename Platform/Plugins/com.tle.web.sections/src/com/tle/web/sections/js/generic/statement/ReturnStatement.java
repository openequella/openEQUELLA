package com.tle.web.sections.js.generic.statement;

import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.JSStatements;
import com.tle.web.sections.js.JSUtils;

public class ReturnStatement implements JSStatements
{
	private final JSExpression expression;

	public ReturnStatement(Object expr)
	{
		this.expression = JSUtils.convertExpression(expr);
	}

	public ReturnStatement(JSExpression expr)
	{
		this.expression = expr;
		if( expr == null )
		{
			throw new NullPointerException();
		}
	}

	@Override
	public String getStatements(RenderContext info)
	{
		return "return " + expression.getExpression(info) + ";"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		info.preRender(expression);
	}
}
