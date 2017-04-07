package com.tle.web.sections.js.generic.expression;

import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.JSExpression;

/**
 * @author aholland
 */
public class SimpleBooleanExpression extends BooleanExpression
{
	protected JSExpression unary;

	public SimpleBooleanExpression(JSExpression unary)
	{
		this.unary = unary;
	}

	@Override
	public String getExpression(RenderContext info)
	{
		return unary.getExpression(info);
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		info.preRender(unary);
	}
}
