package com.tle.web.sections.js.generic.expression;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.JSPropertyExpression;

@NonNullByDefault
public class CombinedPropertyExpression extends AbstractExpression implements JSPropertyExpression
{
	private final JSExpression first;
	private final JSExpression second;

	public CombinedPropertyExpression(JSExpression first, JSExpression second)
	{
		this.first = first;
		this.second = second;
	}

	@Override
	public String getExpression(@Nullable RenderContext info)
	{
		return first.getExpression(info) + second.getExpression(info);
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		SectionUtils.preRender(info, first, second);
	}
}
