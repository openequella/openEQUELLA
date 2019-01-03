/*
 * Licensed to the Apereo Foundation under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.tle.web.sections.standard.js.impl;

import java.util.LinkedHashMap;
import java.util.Map;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.JSAssignable;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.JSPropertyExpression;
import com.tle.web.sections.js.JSStatements;
import com.tle.web.sections.js.generic.expression.CombinedExpression;
import com.tle.web.sections.js.generic.expression.ScriptExpression;
import com.tle.web.sections.js.generic.expression.ScriptVariable;
import com.tle.web.sections.js.generic.function.AbstractCallable;
import com.tle.web.sections.js.generic.function.FunctionDefinition;
import com.tle.web.sections.js.generic.function.FunctionDefinitionStatement;
import com.tle.web.sections.js.generic.statement.AssignStatement;
import com.tle.web.sections.js.generic.statement.DeclarationStatement;
import com.tle.web.sections.js.generic.statement.ReturnStatement;
import com.tle.web.sections.js.generic.statement.StatementBlock;
import com.tle.web.sections.standard.js.JSONComponentMappings;
import com.tle.web.sections.standard.js.modules.JSONModule;

@NonNullByDefault
public class CollectJSONFunction extends AbstractCallable implements JSAssignable, FunctionDefinition
{
	private Map<JSPropertyExpression, JSExpression> mappings = new LinkedHashMap<JSPropertyExpression, JSExpression>();
	private final String name;

	public CollectJSONFunction(String name, JSONComponentMappings jsonMap)
	{
		this.name = name;
		mappings = jsonMap.createGetMappings();
	}

	@Override
	protected String getCallExpression(RenderContext info, JSExpression[] params)
	{
		return name + "()"; //$NON-NLS-1$
	}

	@Override
	public int getNumberOfParams(@Nullable RenderContext context)
	{
		return 0;
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		info.addStatements(new FunctionDefinitionStatement(this));
	}

	@Override
	public JSStatements createFunctionBody(@Nullable RenderContext context, JSExpression[] params)
	{
		StatementBlock statementBlock = new StatementBlock();
		statementBlock.setSeperate(true);
		ScriptVariable newObj = new ScriptVariable("obj"); //$NON-NLS-1$
		statementBlock.addStatements(new DeclarationStatement(newObj, new ScriptExpression("{}"))); //$NON-NLS-1$
		for( JSPropertyExpression key : mappings.keySet() )
		{
			statementBlock.addStatements(new AssignStatement(new CombinedExpression(newObj, key), mappings.get(key)));
		}
		statementBlock.addStatements(new ReturnStatement(JSONModule.getStringifyExpression(newObj)));
		return statementBlock;
	}

	@Nullable
	@Override
	public JSExpression[] getFunctionParams(@Nullable RenderContext context)
	{
		return null;
	}

	@Override
	public String getFunctionName(@Nullable RenderContext context)
	{
		return name;
	}

	@Override
	public String getExpression(@Nullable RenderContext info)
	{
		return name;
	}
}
