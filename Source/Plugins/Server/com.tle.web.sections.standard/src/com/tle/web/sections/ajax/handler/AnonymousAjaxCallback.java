package com.tle.web.sections.ajax.handler;

import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSStatements;
import com.tle.web.sections.js.generic.function.AnonymousFunction;
import com.tle.web.sections.js.generic.statement.FunctionCallStatement;

public class AnonymousAjaxCallback extends AnonymousFunction
{

	public AnonymousAjaxCallback(JSStatements body)
	{
		super(body, AjaxGenerator.RESULTS_VAR, AjaxGenerator.STATUS_VAR);
	}

	public AnonymousAjaxCallback(JSCallable callable, Object... args)
	{
		super(new FunctionCallStatement(callable, args), AjaxGenerator.RESULTS_VAR, AjaxGenerator.STATUS_VAR);
	}
}
