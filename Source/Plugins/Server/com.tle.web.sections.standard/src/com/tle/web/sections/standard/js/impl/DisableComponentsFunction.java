package com.tle.web.sections.standard.js.impl;

import com.tle.web.sections.js.JSStatements;
import com.tle.web.sections.js.generic.expression.ScriptVariable;
import com.tle.web.sections.js.generic.function.SimpleFunction;
import com.tle.web.sections.js.generic.statement.FunctionCallStatement;
import com.tle.web.sections.js.generic.statement.StatementBlock;
import com.tle.web.sections.standard.js.JSDisableable;

public class DisableComponentsFunction extends SimpleFunction
{
	private static final ScriptVariable DISABLE_PARAM = new ScriptVariable("disable"); //$NON-NLS-1$

	public DisableComponentsFunction(String name, JSDisableable... components)
	{
		super(name, createScript(components), DISABLE_PARAM);
	}

	private static JSStatements createScript(JSDisableable... components)
	{
		StatementBlock block = new StatementBlock();
		for( JSDisableable disableable : components )
		{
			block.addStatements(new FunctionCallStatement(disableable.createDisableFunction(), DISABLE_PARAM));
		}
		return block;
	}
}
