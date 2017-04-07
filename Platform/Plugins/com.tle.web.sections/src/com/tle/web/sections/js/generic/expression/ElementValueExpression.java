package com.tle.web.sections.js.generic.expression;

import com.tle.web.sections.js.ElementId;

public class ElementValueExpression extends CombinedExpression
{
	public ElementValueExpression(JSElementExpression exp)
	{
		super(exp, new PropertyExpression("value")); //$NON-NLS-1$
	}

	public ElementValueExpression(ElementId id)
	{
		super(new ElementByIdExpression(id), new PropertyExpression("value")); //$NON-NLS-1$
	}
}
