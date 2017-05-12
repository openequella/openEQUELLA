package com.tle.web.sections.js.generic.expression;

import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.ElementId;
import com.tle.web.sections.js.JSUtils;

public class ElementIdExpression extends AbstractExpression
{
	private final ElementId id;

	public ElementIdExpression(ElementId id)
	{
		this.id = id;
		id.registerUse();
	}

	@Override
	public String getExpression(RenderContext info)
	{
		return JSUtils.toJSString(id.getElementId(info));
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		// nothing
	}

}
