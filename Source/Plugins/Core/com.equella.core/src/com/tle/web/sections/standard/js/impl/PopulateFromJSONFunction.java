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
import java.util.Set;

import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.JSAssignable;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.JSPropertyExpression;
import com.tle.web.sections.js.JSStatements;
import com.tle.web.sections.js.JSUtils;
import com.tle.web.sections.js.generic.expression.ArrayIndexExpression;
import com.tle.web.sections.js.generic.expression.CombinedExpression;
import com.tle.web.sections.js.generic.expression.NotExpression;
import com.tle.web.sections.js.generic.expression.ObjectExpression;
import com.tle.web.sections.js.generic.expression.ScriptVariable;
import com.tle.web.sections.js.generic.function.FunctionDefinition;
import com.tle.web.sections.js.generic.function.FunctionDefinitionStatement;
import com.tle.web.sections.js.generic.statement.AssignStatement;
import com.tle.web.sections.js.generic.statement.DeclarationStatement;
import com.tle.web.sections.js.generic.statement.FunctionCallStatement;
import com.tle.web.sections.js.generic.statement.IfStatement;
import com.tle.web.sections.js.generic.statement.StatementBlock;
import com.tle.web.sections.standard.js.JSONComponentMappings;
import com.tle.web.sections.standard.js.modules.JSONModule;

public class PopulateFromJSONFunction implements JSAssignable, JSCallable, FunctionDefinition
{
	private Map<JSPropertyExpression, JSCallable> mappings = new LinkedHashMap<JSPropertyExpression, JSCallable>();
	private final String name;
	private final Set<String> nullCheckNames;
	private JSStatements extraStatements;
	private static ScriptVariable PARAM_jsonText = new ScriptVariable("jsonText"); //$NON-NLS-1$
	private static ScriptVariable VAR_newObj = new ScriptVariable("obj"); //$NON-NLS-1$
	private static JSExpression[] paramDefs = new JSExpression[]{PARAM_jsonText};

	public PopulateFromJSONFunction(String name, JSONComponentMappings jsonMap)
	{
		this.name = name;
		mappings = jsonMap.createSetMappings();
		nullCheckNames = jsonMap.getMapAttributeNames();
	}

	@Override
	public String getExpressionForCall(RenderContext info, JSExpression... params)
	{
		return JSUtils.createFunctionCall(info, name, params);
	}

	@Override
	public int getNumberOfParams(RenderContext context)
	{
		return 1;
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		info.addStatements(new FunctionDefinitionStatement(this));
	}

	@Override
	public JSStatements createFunctionBody(RenderContext context, JSExpression[] params)
	{
		StatementBlock statementBlock = new StatementBlock();

		//$NON-NLS-1$
		statementBlock
			.addStatements(new DeclarationStatement(VAR_newObj, JSONModule.getParseExpression(PARAM_jsonText)));

		for( String mapName : nullCheckNames )
		{
			CombinedExpression mapExpr = ArrayIndexExpression.create(VAR_newObj, mapName);
			statementBlock.addStatements(new IfStatement(new NotExpression(mapExpr), new AssignStatement(mapExpr,
				new ObjectExpression())));
		}
		for( JSPropertyExpression key : mappings.keySet() )
		{
			statementBlock.addStatements(new FunctionCallStatement(mappings.get(key), new CombinedExpression(
				VAR_newObj, key)));
		}
		if( extraStatements != null )
		{
			statementBlock.addStatements(extraStatements);
		}
		return statementBlock;
	}

	@Override
	public JSExpression[] getFunctionParams(RenderContext context)
	{
		return paramDefs;
	}

	@Override
	public String getFunctionName(RenderContext context)
	{
		return name;
	}

	@Override
	public String getExpression(RenderContext info)
	{
		return name;
	}

	public JSStatements getExtraStatements()
	{
		return extraStatements;
	}

	public void addExtraStatements(JSStatements extraStatements)
	{
		if( this.extraStatements == null )
		{
			this.extraStatements = extraStatements;
		}
		else
		{
			StatementBlock statementBlock = new StatementBlock();
			statementBlock.addStatements(this.extraStatements);
			statementBlock.addStatements(extraStatements);
			this.extraStatements = statementBlock;
		}
	}
}
