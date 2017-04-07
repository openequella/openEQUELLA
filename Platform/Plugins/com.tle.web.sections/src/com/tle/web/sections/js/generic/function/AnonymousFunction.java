package com.tle.web.sections.js.generic.function;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.JSAssignable;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.JSStatements;
import com.tle.web.sections.js.generic.statement.FunctionCallStatement;

@NonNullByDefault
@SuppressWarnings("nls")
public class AnonymousFunction extends AbstractFunctionDefinition implements JSAssignable
{
	public AnonymousFunction(JSStatements body, JSExpression... params)
	{
		this.body = body;
		this.params = params;
	}

	public AnonymousFunction(JSCallable call, Object... params)
	{
		this(new FunctionCallStatement(call, params), new JSExpression[]{});
	}

	@Override
	public String getExpression(@Nullable RenderContext info)
	{
		return getDefinition(info);
	}

	@Override
	public int getNumberOfParams(@Nullable RenderContext context)
	{
		return params.length;
	}

	@Nullable
	@Override
	protected String getFunctionName(@Nullable RenderContext context)
	{
		return null;
	}

	@Override
	public String toString()
	{
		return "function(){" + body + "}";
	}
}
