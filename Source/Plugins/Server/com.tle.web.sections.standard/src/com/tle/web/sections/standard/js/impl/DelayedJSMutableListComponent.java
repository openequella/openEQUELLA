package com.tle.web.sections.standard.js.impl;

import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.ElementId;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.function.RuntimeFunction;
import com.tle.web.sections.standard.js.JSMutableListComponent;

public class DelayedJSMutableListComponent extends DelayedJSListComponent<JSMutableListComponent>
{
	private RuntimeFunction addFunction;
	private RuntimeFunction removeFunction;

	public DelayedJSMutableListComponent(ElementId id)
	{
		super(id);
	}

	public JSCallable createAddFunction()
	{
		if( addFunction == null )
		{
			addFunction = new DelayedFunction<JSMutableListComponent>(this, "add", id, 2) //$NON-NLS-1$
			{
				@Override
				protected JSCallable createRealFunction(RenderContext info, JSMutableListComponent renderer)
				{
					return renderer.createAddFunction();
				}
			};
		}
		return addFunction;
	}

	public JSCallable createRemoveFunction()
	{
		if( removeFunction == null )
		{
			removeFunction = new DelayedFunction<JSMutableListComponent>(this, "remove", id, 0) //$NON-NLS-1$
			{
				@Override
				protected JSCallable createRealFunction(RenderContext info, JSMutableListComponent renderer)
				{
					return renderer.createRemoveFunction();
				}

			};
		}
		return removeFunction;
	}

}
