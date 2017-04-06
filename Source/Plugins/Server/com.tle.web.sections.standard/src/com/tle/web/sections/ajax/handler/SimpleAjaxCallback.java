package com.tle.web.sections.ajax.handler;

import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.js.ElementId;
import com.tle.web.sections.js.JSStatements;
import com.tle.web.sections.js.generic.function.SimpleFunction;

public class SimpleAjaxCallback extends SimpleFunction
{
	public SimpleAjaxCallback(String name, ElementId id, JSStatements script)
	{
		super(name, id, script, AjaxGenerator.RESULTS_VAR, AjaxGenerator.STATUS_VAR);
	}

	public SimpleAjaxCallback(String name, JSStatements script)
	{
		this(name, null, script);
	}
}
