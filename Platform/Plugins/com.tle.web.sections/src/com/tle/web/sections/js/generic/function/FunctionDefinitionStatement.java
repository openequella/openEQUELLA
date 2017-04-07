package com.tle.web.sections.js.generic.function;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.JSStatements;

@NonNullByDefault
public class FunctionDefinitionStatement extends AbstractFunctionDefinition implements JSStatements
{
	private final FunctionDefinition definition;

	public FunctionDefinitionStatement(FunctionDefinition definition)
	{
		this.definition = definition;
	}

	@Override
	public String getStatements(@Nullable RenderContext info)
	{
		return getDefinition(info);
	}

	@Override
	protected JSStatements getBody(@Nullable RenderContext context)
	{
		if( body == null )
		{
			body = definition.createFunctionBody(context, getParams(context));
		}
		return body;
	}

	@Override
	protected JSExpression[] getParams(@Nullable RenderContext context)
	{
		if( params == null )
		{
			params = definition.getFunctionParams(context);
		}
		return params;
	}

	@Override
	protected String getFunctionName(@Nullable RenderContext context)
	{
		return definition.getFunctionName(context);
	}
}
