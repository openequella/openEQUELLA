package com.tle.web.sections.standard.js.impl;

import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.ElementId;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.standard.js.JSTableComponent;

public class DelayedJSTableComponent extends AbstractDelayedJS<JSTableComponent>
{
	private JSCallable filterFunction;

	public DelayedJSTableComponent(ElementId id)
	{
		super(id);
	}

	public JSCallable createFilterFunction()
	{
		if( filterFunction == null )
		{
			filterFunction = new DelayedFunction<JSTableComponent>(this, "filter", id, 1) //$NON-NLS-1$
			{
				@Override
				protected JSCallable createRealFunction(RenderContext info, JSTableComponent renderer)
				{
					return renderer.createFilterFunction();
				}
			};
		}
		return filterFunction;
	}
}
