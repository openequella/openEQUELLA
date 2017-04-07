package com.tle.web.sections.js.generic.expression;

import com.tle.annotation.NonNullByDefault;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;

/**
 * @author aholland
 */
@NonNullByDefault
public class ParentFrameElementExpression extends AbstractExpression implements JSElementExpression
{
	private final JSElementExpression parentExpression;

	public ParentFrameElementExpression(JSElementExpression parentExpression)
	{
		this.parentExpression = parentExpression;
	}

	@Override
	public String getExpression(RenderContext info)
	{
		return "self.parent." + parentExpression.getExpression(info); //$NON-NLS-1$
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		// nada
	}
}
