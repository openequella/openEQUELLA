package com.tle.common.scripting;

import com.dytech.edge.common.ScriptContext;

/**
 * @author aholland
 */
public interface ScriptEvaluator
{
	boolean evaluateScript(String script, String scriptName, ScriptContext context) throws ScriptException;

	Object executeScript(String script, String scriptName, ScriptContext context, boolean function)
		throws ScriptException;

	Object executeScript(String script, String scriptName, ScriptContext context, boolean function,
		Class<?> expectedReturnClass) throws ScriptException;
}
