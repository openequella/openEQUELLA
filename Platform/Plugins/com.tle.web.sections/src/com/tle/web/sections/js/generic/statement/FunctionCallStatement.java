/*
 * Copyright 2017 Apereo
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

package com.tle.web.sections.js.generic.statement;

import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSStatements;
import com.tle.web.sections.js.generic.expression.FunctionCallExpression;

public class FunctionCallStatement implements JSStatements
{
	protected FunctionCallExpression expr;

	public FunctionCallStatement(FunctionCallExpression expr)
	{
		this.expr = expr;
	}

	public FunctionCallStatement(String name, Object... exprs)
	{
		this(new FunctionCallExpression(name, exprs));
	}

	public FunctionCallStatement(JSCallable func, Object... exprs)
	{
		this(new FunctionCallExpression(func, exprs));
	}

	@Override
	public String getStatements(RenderContext info)
	{
		return expr.getExpression(info) + ';';
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		info.preRender(expr);
	}

	@Override
	public String toString()
	{
		return expr.toString() + ';';
	}

	public static FunctionCallStatement jscall(FunctionCallExpression expr)
	{
		return new FunctionCallStatement(expr);
	}

	public static FunctionCallStatement jscall(JSCallable func, Object... params)
	{
		return new FunctionCallStatement(func, params);
	}
}
