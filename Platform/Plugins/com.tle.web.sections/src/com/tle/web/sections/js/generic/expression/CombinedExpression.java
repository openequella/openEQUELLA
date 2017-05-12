package com.tle.web.sections.js.generic.expression;

import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.JSExpression;

public class CombinedExpression extends AbstractExpression
{
	private JSExpression first;
	private JSExpression second;

	public CombinedExpression(JSExpression first, JSExpression second)
	{
		this.first = first;
		this.second = second;
	}

	@Override
	public String getExpression(RenderContext info)
	{
		return first.getExpression(info) + second.getExpression(info);
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		info.preRender(first, second);
	}
}
