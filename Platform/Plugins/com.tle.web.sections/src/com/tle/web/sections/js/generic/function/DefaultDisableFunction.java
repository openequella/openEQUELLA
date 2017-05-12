package com.tle.web.sections.js.generic.function;

import com.tle.web.sections.js.ElementId;
import com.tle.web.sections.js.generic.expression.ElementByIdExpression;
import com.tle.web.sections.js.generic.expression.PropertyExpression;
import com.tle.web.sections.js.generic.statement.AssignAsFunction;

/**
 * @author aholland
 */
public class DefaultDisableFunction extends AssignAsFunction
{
	public DefaultDisableFunction(ElementId element)
	{
		super(PropertyExpression.create(new ElementByIdExpression(element), "disabled")); //$NON-NLS-1$
	}
}
