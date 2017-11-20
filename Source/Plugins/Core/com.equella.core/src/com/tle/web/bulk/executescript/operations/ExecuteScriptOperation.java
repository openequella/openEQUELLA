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

package com.tle.web.bulk.executescript.operations;

import com.dytech.edge.common.ScriptContext;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.common.scripting.service.ScriptingService;
import com.tle.core.item.operations.AbstractWorkflowOperation;
import com.tle.core.scripting.service.StandardScriptContextParams;
import com.tle.core.security.impl.SecureOnCall;

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
		StandardScriptContextParams params = new StandardScriptContextParams(getItemPack(), getStaging(), false, null);

		ScriptContext scriptContext = scriptService.createScriptContext(params);
		scriptService.executeScript(script, "bulkExecute", scriptContext, false);
		return true;
	}

}
