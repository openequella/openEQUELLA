package com.tle.web.sections.js.generic.expression;

import com.google.gson.Gson;
import com.tle.annotation.NonNullByDefault;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.JSUtils;

@NonNullByDefault
public class JSONStringExpression implements JSExpression
{
	private static Gson gson = new Gson();
	private Object obj;

	public JSONStringExpression(Object obj)
	{
		this.obj = obj;
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		// nothing
	}

	@Override
	public String getExpression(RenderContext info)
	{
		return JSUtils.toJSString(gson.toJson(obj));
	}

}
