package com.tle.web.sections.events.js;

import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.generic.function.PrependedParameterFunction;

public class SubmitValuesFunction extends PrependedParameterFunction
{
	private SubmitEventFunction eventFunc;
	// private String firstParam;
	// private int numParams;
	private final ParameterizedEvent event;

	public SubmitValuesFunction(ParameterizedEvent event)
	{
		super(new SubmitEventFunction(), event.getEventId());
		this.eventFunc = (SubmitEventFunction) func;
		// this.firstParam = pevent.getEventId();
		// this.numParams = pevent.getParameterCount();
		this.event = event;
	}

	public String getFirstParam()
	{
		// return firstParam;
		return event.getEventId();
	}

	public void setValidate(boolean validate)
	{
		eventFunc.setValidate(validate);
	}

	public void setBlockFurtherSubmission(boolean blockFurtherSubmission)
	{
		eventFunc.setBlockFurtherSubmission(blockFurtherSubmission);
	}

	@Override
	public int getNumberOfParams(RenderContext context)
	{
		// return numParams;
		return event.getParameterCount();
	}

	public SubmitEventFunction getSubmitEventFunction()
	{
		return eventFunc;
	}

	public ParameterizedEvent getEvent()
	{
		return event;
	}
}