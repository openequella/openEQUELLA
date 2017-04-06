package com.tle.core.scripting;

import java.util.Map;

import com.tle.common.scripting.service.ScriptContextCreationParams;

/**
 * @author aholland
 */
public interface WorkflowScriptObjectContributor // extends
// ScriptObjectContributor
{
	void addWorkflowScriptObjects(Map<String, Object> objects, ScriptContextCreationParams params);
}
