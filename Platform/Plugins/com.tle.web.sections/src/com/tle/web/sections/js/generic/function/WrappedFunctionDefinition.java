package com.tle.web.sections.js.generic.function;

import com.tle.web.sections.SectionsRuntimeException;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.ElementId;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.JSStatements;
import com.tle.web.sections.js.JSUtils;
import com.tle.web.sections.js.generic.statement.FunctionCallStatement;
import com.tle.web.sections.js.generic.statement.StatementBlock;

public class WrappedFunctionDefinition implements FunctionDefinition
{
	private final String name;
	private final ElementId elementId;
	private final JSCallable callable;
	private final JSStatements[] startStatements;

	public WrappedFunctionDefinition(String name, ElementId id, JSCallable callable, JSStatements... startStatements)
	{
		this.elementId = id;
		this.name = name;
		this.callable = callable;
		this.startStatements = startStatements;
	}

	@Override
	public JSStatements createFunctionBody(RenderContext context, JSExpression[] params)
	{
		FunctionCallStatement call = new FunctionCallStatement(callable, (Object[]) params);
		if( startStatements != null )
		{
			JSStatements block = StatementBlock.get(startStatements);
			return StatementBlock.get(block, call);
		}
		return call;
	}

	@Override
	public String getFunctionName(RenderContext context)
	{
		if( elementId == null )
		{
			return name;
		}
		return name + elementId.getElementId(context);
	}

	@Override
	public JSExpression[] getFunctionParams(RenderContext context)
	{
		int numParams = callable.getNumberOfParams(context);
		if( numParams < 0 )
		{
			throw new SectionsRuntimeException("Unknown number of parameters for " + name); //$NON-NLS-1$
		}
		return JSUtils.createParameters(numParams);
	}

}
