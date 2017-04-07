package com.tle.web.sections.js.validators;

import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.render.Label;

public class Confirm extends FunctionCallValidator
{
	public static final ExternallyDefinedFunction CONFIRM = new ExternallyDefinedFunction("confirm", 1); //$NON-NLS-1$

	public Confirm(Label label)
	{
		super(CONFIRM, label);
	}
}
