package com.tle.web.sections.js.generic.expression;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.JSPropertyExpression;
import com.tle.web.sections.js.JSUtils;

@NonNullByDefault
public class ArrayIndexExpression extends AbstractExpression implements JSPropertyExpression
{
	private final JSExpression indexExpr;

	public ArrayIndexExpression(Object index)
	{
		this.indexExpr = JSUtils.convertExpressions(index)[0];
	}

	@Override
	public String getExpression(@Nullable RenderContext info)
	{
		return '[' + indexExpr.getExpression(info) + ']';
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		SectionUtils.preRender(info, indexExpr);
	}

	public static CombinedExpression create(JSExpression base, Object index)
	{
		return new CombinedExpression(base, new ArrayIndexExpression(index));
	}
}
