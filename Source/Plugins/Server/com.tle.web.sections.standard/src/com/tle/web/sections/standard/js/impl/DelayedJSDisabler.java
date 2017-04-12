package com.tle.web.sections.standard.js.impl;

import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.ElementId;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.function.RuntimeFunction;
import com.tle.web.sections.standard.js.JSDisableable;
import com.tle.web.sections.standard.model.HtmlComponentState;

public class DelayedJSDisabler extends AbstractDelayedJS<JSDisableable> implements JSDisableable
{
	private RuntimeFunction disabler;

	public DelayedJSDisabler(final ElementId id)
	{
		super(id);
		id.registerUse();
	}

	public DelayedJSDisabler(HtmlComponentState state)
	{
		this((ElementId) state);
		state.addRendererCallback(this);
	}

	@Override
	public JSCallable createDisableFunction()
	{
		if( disabler == null )
		{
			disabler = new DelayedFunction<JSDisableable>(this, "ddis", id, 1) //$NON-NLS-1$
			{
				@Override
				protected JSCallable createRealFunction(RenderContext info, JSDisableable renderer)
				{
					return renderer.createDisableFunction();
				}
			};
		}
		return disabler;
	}
}
