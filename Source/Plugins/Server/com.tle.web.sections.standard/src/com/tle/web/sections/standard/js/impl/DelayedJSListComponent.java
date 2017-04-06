package com.tle.web.sections.standard.js.impl;

import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.ElementId;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.generic.expression.RuntimeExpression;
import com.tle.web.sections.js.generic.function.RuntimeFunction;
import com.tle.web.sections.standard.js.JSListComponent;

public class DelayedJSListComponent<M extends JSListComponent> extends DelayedJSValueComponent<M>
{
	private RuntimeExpression notEmpty;
	private RuntimeExpression getName;
	private RuntimeFunction setAll;

	public DelayedJSListComponent(ElementId id)
	{
		super(id);
	}

	public JSExpression createNotEmptyExpression()
	{
		if( notEmpty == null )
		{
			notEmpty = new DelayedExpression<M>(this, "notEmpty", id) //$NON-NLS-1$
			{
				@Override
				protected JSExpression createRealExpression(SectionInfo info, M renderer)
				{
					return renderer.createNotEmptyExpression();
				}
			};
		}
		return notEmpty;
	}

	public JSExpression createGetNameExpression()
	{
		if( getName == null )
		{
			getName = new DelayedExpression<M>(this, "name", id) //$NON-NLS-1$
			{
				@Override
				protected JSExpression createRealExpression(SectionInfo info, M renderer)
				{
					return renderer.createGetNameExpression();
				}
			};
		}
		return getName;
	}

	public JSCallable createSetAllFunction()
	{
		if( setAll == null )
		{
			setAll = new DelayedFunction<M>(this, "setAll", id, 1) //$NON-NLS-1$
			{
				@Override
				protected JSCallable createRealFunction(RenderContext info, M renderer)
				{
					return renderer.createSetAllFunction();
				}
			};
		}
		return setAll;
	}
}
