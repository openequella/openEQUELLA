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

package com.tle.web.sections.js.generic;

import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.js.JSBookmarkModifier;
import com.tle.web.sections.js.JSCallAndReference;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSStatements;
import com.tle.web.sections.js.generic.statement.FunctionCallStatement;
import com.tle.web.sections.js.generic.statement.StatementBlock;

public class StatementHandler implements JSHandler
{
	private JSStatements validators;
	private JSStatements statements;
	private JSCallAndReference handlerFunc;
	private boolean overrideDefault;

	@Override
	public JSStatements getValidators()
	{
		return validators;
	}

	public StatementHandler(JSCallAndReference handlerFunc)
	{
		this((JSCallable) handlerFunc);
	}

	public StatementHandler(JSCallable callable, Object... args)
	{
		if( args.length == 0 && callable instanceof JSCallAndReference )
		{
			this.handlerFunc = (JSCallAndReference) callable;
		}
		statements = new FunctionCallStatement(callable, args);
	}

	public StatementHandler(JSHandler handler, JSStatements statements)
	{
		this.statements = StatementBlock.get(handler.getStatements(), statements);
		this.validators = StatementBlock.get(handler.getValidators());
		this.overrideDefault = handler.isOverrideDefault();
	}

	public StatementHandler(JSStatements statements)
	{
		this.statements = statements;
	}

	public StatementHandler(JSStatements... statements)
	{
		this.statements = StatementBlock.get(statements);
	}

	@Override
	public String getStatements(RenderContext info)
	{
		StringBuilder sbuf = new StringBuilder();
		if( validators != null )
		{
			sbuf.append(validators.getStatements(info));
		}
		if( statements != null )
		{
			sbuf.append(statements.getStatements(info));
		}
		return sbuf.toString();
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		info.preRender(validators, statements);
	}

	@Override
	public JSHandler addValidator(JSStatements validator)
	{
		validators = StatementBlock.get(validators, validator);
		return this;
	}

	@Override
	public JSHandler addStatements(JSStatements statement)
	{
		statements = StatementBlock.get(statements, statement);
		handlerFunc = null;
		return this;
	}

	@Override
	public JSCallAndReference getW3CHandler()
	{
		return null;
	}

	@Override
	public JSCallAndReference getHandlerFunction()
	{
		return handlerFunc;
	}

	@Override
	public JSBookmarkModifier getModifier()
	{
		return null;
	}

	@Override
	public boolean isOverrideDefault()
	{
		return overrideDefault;
	}

	@Override
	public JSStatements getStatements()
	{
		return statements;
	}
}
