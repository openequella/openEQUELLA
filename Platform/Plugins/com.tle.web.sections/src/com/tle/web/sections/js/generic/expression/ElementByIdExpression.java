package com.tle.web.sections.js.generic.expression;

import com.tle.annotation.NonNullByDefault;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.header.HeaderHelper;
import com.tle.web.sections.js.ElementId;
import com.tle.web.sections.js.JSUtils;

@NonNullByDefault
public class ElementByIdExpression extends AbstractExpression implements JSElementExpression
{
	private final ElementId elementId;
	private final String property;

	public ElementByIdExpression(ElementId elementId, String property)
	{
		this.elementId = elementId;
		this.property = property;
		elementId.registerUse();
	}

	public ElementByIdExpression(ElementId elementId)
	{
		this(elementId, ""); //$NON-NLS-1$
	}

	@Override
	public String getExpression(RenderContext info)
	{
		HeaderHelper helper = info.getHelper();
		return JSUtils.getElement(helper.getElementFunction(), elementId.getElementId(info)).getExpression(info)
			+ property;
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		info.preRender(info.getHelper().getElementFunction());
	}
}
