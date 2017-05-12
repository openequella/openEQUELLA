package com.tle.web.sections.js.generic.function;

import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.JSCallable;

public class ReloadFunction extends RuntimeFunction
{
	private final boolean validate;

	public ReloadFunction()
	{
		this(true);
	}

	public ReloadFunction(boolean validate)
	{
		this.validate = validate;
	}

	@Override
	protected JSCallable createFunction(RenderContext info)
	{
		return info.getHelper().getSubmitFunction(validate, false, true);
	}

	@Override
	public int getNumberOfParams(RenderContext context)
	{
		return 0;
	}
}
