package com.tle.web.sections.js.validators;

import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.expression.FunctionCallExpression;

public class FunctionCallValidator extends SimpleValidator
{
	public FunctionCallValidator(JSCallable function, Object... params)
	{
		super(new FunctionCallExpression(function, params));
	}
}
