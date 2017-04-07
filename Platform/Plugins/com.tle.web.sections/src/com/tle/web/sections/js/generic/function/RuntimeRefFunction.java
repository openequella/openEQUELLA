package com.tle.web.sections.js.generic.function;

import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.JSCallAndReference;

public class RuntimeRefFunction extends RuntimeFunction implements JSCallAndReference
{
	@Override
	public String getExpression(RenderContext info)
	{
		return getRefFunction(info).getExpression(info);
	}

	private JSCallAndReference getRefFunction(RenderContext info)
	{
		return (JSCallAndReference) getRealFunction(info);
	}

	@Override
	public boolean isStatic()
	{
		return false;
	}
}
