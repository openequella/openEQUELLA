package com.tle.web.sections.standard.js.impl;

import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionsRuntimeException;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.ElementId;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.JSStatements;
import com.tle.web.sections.js.generic.expression.FunctionCallExpression;
import com.tle.web.sections.js.generic.expression.RuntimeExpression;
import com.tle.web.sections.js.generic.function.DynamicNamedFunction;
import com.tle.web.sections.js.generic.statement.ReturnStatement;
import com.tle.web.sections.standard.js.DelayedRenderer;

public abstract class DelayedExpression<T> extends RuntimeExpression
{
	private final DelayedRenderer<T> delayedRenderer;
	private final ElementId id;
	private final String prefix;

	public DelayedExpression(DelayedRenderer<T> delayedRenderer, String prefix, ElementId id)
	{
		this.delayedRenderer = delayedRenderer;
		this.prefix = prefix;
		this.id = id;
	}

	@Override
	protected JSExpression createExpression(RenderContext info)
	{
		T renderer = delayedRenderer.getSelectedRenderer(info);
		if( renderer != null )
		{
			return createRealExpression(info, renderer);
		}
		return new FunctionCallExpression(new DynamicNamedFunction(prefix, id)
		{
			@SuppressWarnings("nls")
			@Override
			public JSStatements createFunctionBody(RenderContext context, JSExpression[] params)
			{
				T renderer = delayedRenderer.getSelectedRenderer(context);
				if( renderer == null )
				{
					throw new SectionsRuntimeException("Trying to use function '" + prefix + id.getElementId(context)
						+ "' but failed to render component with id:" + id.getElementId(context));
				}
				return new ReturnStatement(createRealExpression(context, renderer));
			}
		});
	}

	protected abstract JSExpression createRealExpression(SectionInfo info, T renderer);
}
