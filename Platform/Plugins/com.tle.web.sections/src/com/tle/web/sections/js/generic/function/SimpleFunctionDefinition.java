package com.tle.web.sections.js.generic.function;

import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.ElementId;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.JSStatements;

public class SimpleFunctionDefinition implements FunctionDefinition
{
	private final String name;
	private final ElementId elementId;
	private final JSStatements body;
	private final JSExpression[] paramDefs;

	public SimpleFunctionDefinition(String name, ElementId elementId, JSStatements body, JSExpression[] paramdefs)
	{
		this.name = name;
		this.elementId = elementId;
		this.body = body;
		this.paramDefs = paramdefs;
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
	public JSStatements createFunctionBody(RenderContext context, JSExpression[] params)
	{
		return body;
	}

	@Override
	public JSExpression[] getFunctionParams(RenderContext context)
	{
		return paramDefs;
	}
}
