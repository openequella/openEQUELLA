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

package com.tle.web.sections.js.generic.function;

import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.ElementId;
import com.tle.web.sections.js.JSCallAndReference;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.JSStatements;
import com.tle.web.sections.js.JSUtils;

/**
 * An immutable programattically defined Javascript function.
 * 
 * @author jolz
 */
public class SimpleFunction implements JSCallAndReference
{
	private final FunctionDefinition definition;
	protected int numParams;
	protected boolean staticName;

	public SimpleFunction(FunctionDefinition definition)
	{
		this.definition = definition;
	}

	public SimpleFunction(String name, ElementId id, JSStatements script, JSExpression... paramdefs)
	{
		this(new SimpleFunctionDefinition(name, id, script, paramdefs));
		staticName = id == null;
		this.numParams = paramdefs != null ? paramdefs.length : 0;
	}

	@Override
	public boolean isStatic()
	{
		return staticName;
	}

	public SimpleFunction(String name, JSStatements script, JSExpression... paramdefs)
	{
		this(name, null, script, paramdefs);
	}

	@Override
	public String getExpression(RenderContext info)
	{
		return definition.getFunctionName(info);
	}

	@Override
	public String getExpressionForCall(RenderContext info, JSExpression... params)
	{
		return JSUtils.createFunctionCall(info, definition.getFunctionName(info), params);
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		info.addStatements(new FunctionDefinitionStatement(definition));
	}

	@Override
	public int getNumberOfParams(RenderContext context)
	{
		return numParams;
	}
}
