package com.tle.web.sections.js.generic.function;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.JSStatements;

@NonNullByDefault
public interface FunctionDefinition
{
	String getFunctionName(@Nullable RenderContext context);

	JSStatements createFunctionBody(@Nullable RenderContext context, JSExpression[] params);

	@Nullable
	JSExpression[] getFunctionParams(@Nullable RenderContext context);
}
