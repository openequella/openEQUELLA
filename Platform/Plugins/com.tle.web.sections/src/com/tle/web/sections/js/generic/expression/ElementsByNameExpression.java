package com.tle.web.sections.js.generic.expression;

import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;

public class ElementsByNameExpression extends FunctionCallExpression
{
	public ElementsByNameExpression(String name)
	{
		super(new ExternallyDefinedFunction("document.getElementsByName"), name); //$NON-NLS-1$		
	}
}
