package com.tle.web.sections.js.generic.expression;

import com.tle.annotation.NonNullByDefault;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.JSPropertyExpression;

@NonNullByDefault
public class PropertyExpression extends AbstractExpression implements JSPropertyExpression
{
	private final JSExpression property;

	public PropertyExpression(String property)
	{
		this.property = new ScriptExpression(property);
	}

	public PropertyExpression(JSExpression property)
	{
		this.property = property;
	}

	@Override
	public String getExpression(RenderContext info)
	{
		return '.' + property.getExpression(info);
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		info.preRender(property);
	}

	public static CombinedExpression create(JSExpression expr, String prop)
	{
		return new CombinedExpression(expr, new PropertyExpression(prop));
	}

	public static CombinedExpression create(JSExpression expr, JSExpression prop)
	{
		return new CombinedExpression(expr, new PropertyExpression(prop));
	}
}
