package com.tle.web.sections.standard.js.impl;

import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.ElementId;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.generic.expression.RuntimeExpression;
import com.tle.web.sections.js.generic.function.RuntimeFunction;
import com.tle.web.sections.standard.js.JSValueComponent;

public class DelayedJSValueComponent<T extends JSValueComponent> extends AbstractDelayedJS<T>
	implements
		JSValueComponent
{
	private RuntimeFunction setFunction;
	private RuntimeExpression getExpression;
	private RuntimeFunction resetFunction;

	public DelayedJSValueComponent(ElementId id)
	{
		super(id);
	}

	@Override
	public JSCallable createSetFunction()
	{
		if( setFunction == null )
		{
			setFunction = new DelayedFunction<T>(this, "set", id, 1) //$NON-NLS-1$
			{
				@Override
				protected JSCallable createRealFunction(RenderContext info, T renderer)
				{
					return renderer.createSetFunction();
				}
			};
		}
		return setFunction;

	}

	@Override
	public JSExpression createGetExpression()
	{
		if( getExpression == null )
		{
			getExpression = new DelayedExpression<T>(this, "get", id) //$NON-NLS-1$
			{
				@Override
				protected JSExpression createRealExpression(SectionInfo info, T renderer)
				{
					return renderer.createGetExpression();

				}
			};
		}
		return getExpression;
	}

	@Override
	public JSCallable createResetFunction()
	{
		if( resetFunction == null )
		{
			resetFunction = new DelayedFunction<T>(this, "reset", id, 0) //$NON-NLS-1$
			{
				@Override
				protected JSCallable createRealFunction(RenderContext info, T renderer)
				{
					return renderer.createResetFunction();
				}
			};
		}
		return resetFunction;
	}
}
