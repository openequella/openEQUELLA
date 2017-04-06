package com.tle.common.scripting.service;

import com.dytech.edge.common.ScriptContext;
import com.tle.common.scripting.ScriptEvaluator;

public interface ScriptingService extends ScriptEvaluator
{
	ScriptContext createScriptContext(ScriptContextCreationParams params);
}