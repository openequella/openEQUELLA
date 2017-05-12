package com.tle.web.sections.js.generic.expression;

import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.JSUtils;
import com.tle.web.sections.js.ServerSideValue;

public class StringExpression extends AbstractExpression implements ServerSideValue
{
	private final String str;

	public StringExpression(String str)
	{
		this.str = str;
	}

	@Override
	public String getExpression(RenderContext info)
	{
		return JSUtils.toJSString(str);
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		// nothing
	}

	@Override
	public String getJavaString()
	{
		return str;
	}
}
