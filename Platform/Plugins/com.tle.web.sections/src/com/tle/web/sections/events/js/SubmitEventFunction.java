package com.tle.web.sections.events.js;

import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.function.RuntimeRefFunction;

public class SubmitEventFunction extends RuntimeRefFunction
{
	private boolean validate;
	private boolean blockFurtherSubmission = true;

	public SubmitEventFunction()
	{
		this(true, true);
	}

	public SubmitEventFunction(boolean validate, boolean blockFurtherSubmission)
	{
		this.validate = validate;
		this.blockFurtherSubmission = blockFurtherSubmission;
	}

	@Override
	protected JSCallable createFunction(RenderContext info)
	{
		return info.getHelper().getSubmitFunction(validate, true, blockFurtherSubmission);
	}

	public void setValidate(boolean validate)
	{
		this.validate = validate;
	}

	public void setBlockFurtherSubmission(boolean blockFurtherSubmission)
	{
		this.blockFurtherSubmission = blockFurtherSubmission;
	}
}
