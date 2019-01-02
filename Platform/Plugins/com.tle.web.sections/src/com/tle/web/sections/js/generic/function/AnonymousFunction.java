/*
 * Copyright 2019 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
