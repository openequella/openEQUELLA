package com.tle.web.sections.js.generic.expression;

import com.tle.web.sections.js.ServerSideValue;

public class PrimitiveValueExpression extends ScriptExpression implements ServerSideValue
{
	public PrimitiveValueExpression(int expr)
	{
		super(Integer.toString(expr));
	}

	public PrimitiveValueExpression(long expr)
	{
		super(Long.toString(expr));
	}

	public PrimitiveValueExpression(double expr)
	{
		super(Double.toString(expr));
	}

	public PrimitiveValueExpression(boolean expr)
	{
		super(Boolean.toString(expr));
	}

	@Override
	public String getJavaString()
	{
		return expr;
	}
}
