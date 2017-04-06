package com.tle.web.bulk.executescript.operations;

import com.dytech.edge.common.ScriptContext;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.common.scripting.service.ScriptingService;
import com.tle.core.scripting.service.StandardScriptContextParams;
import com.tle.core.security.impl.SecureOnCall;
import com.tle.core.workflow.operations.AbstractWorkflowOperation;

@SecureOnCall(priv = "EDIT_ITEM")
public class ExecuteScriptOperation extends AbstractWorkflowOperation
{
	@Inject
	ScriptingService scriptService;
	private final String script;

	@AssistedInject
	public ExecuteScriptOperation(@Assisted("script") String script)
	{
		this.script = script;
	}

	@Override
	public boolean execute()
	{
		StandardScriptContextParams params = new StandardScriptContextParams(getItemPack(), getStaging(),
			false, null);
		
		ScriptContext scriptContext = scriptService.createScriptContext(params);
		scriptService.executeScript(script, "bulkExecute", scriptContext, false);
		return true;
	}

}
