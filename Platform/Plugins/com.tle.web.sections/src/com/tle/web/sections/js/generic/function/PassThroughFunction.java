package com.tle.web.sections.js.generic.function;

import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.ElementId;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSStatements;

public class PassThroughFunction extends SimpleFunction
{

	private JSCallable callable;

	public PassThroughFunction(String name, JSCallable callable, JSStatements... start)
	{
		this(name, null, callable, start);
	}

	public PassThroughFunction(String name, ElementId id, JSCallable callable, JSStatements... start)
	{
		super(new WrappedFunctionDefinition(name, id, callable, start));
		this.callable = callable;
		this.staticName = id == null || id.isStaticId();
	}

	@Override
	public int getNumberOfParams(RenderContext context)
	{
		return callable.getNumberOfParams(context);
	}
}
