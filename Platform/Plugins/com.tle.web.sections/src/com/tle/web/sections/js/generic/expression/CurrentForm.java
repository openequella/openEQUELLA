package com.tle.web.sections.js.generic.expression;

import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.JSExpression;

public final class CurrentForm extends AbstractExpression
{
	public static final JSExpression EXPR = new CurrentForm();

	private CurrentForm()
	{
		// nothing
	}

	@Override
	public String getExpression(RenderContext info)
	{
		JSExpression formExpression = info.getHelper().getFormExpression();
		info.getPreRenderContext().preRender(formExpression);
		return formExpression.getExpression(info);
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		// wait until we actually get rendered
	}
}
