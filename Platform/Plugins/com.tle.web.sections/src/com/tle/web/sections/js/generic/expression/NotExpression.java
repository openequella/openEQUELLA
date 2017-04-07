package com.tle.web.sections.js.generic.expression;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.JSExpression;

@NonNullByDefault
public class NotExpression extends BooleanExpression
{
	private JSExpression inner;

	public NotExpression(JSExpression inner)
	{
		this.inner = inner;
	}

	@Override
	public String getExpression(@Nullable RenderContext info)
	{
		return "!(" + inner.getExpression(info) + ")"; //$NON-NLS-1$//$NON-NLS-2$
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		SectionUtils.preRender(info, inner);
	}
}
