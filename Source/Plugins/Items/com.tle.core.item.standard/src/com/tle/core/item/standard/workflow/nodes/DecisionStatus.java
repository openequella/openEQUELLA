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

package com.tle.core.item.standard.workflow.nodes;

import com.dytech.edge.common.ScriptContext;
import com.dytech.edge.exceptions.WorkflowException;
import com.tle.common.Check;
import com.tle.common.scripting.ScriptEvaluator;
import com.tle.common.scripting.ScriptException;
import com.tle.common.workflow.WorkflowNodeStatus;
import com.tle.common.workflow.node.DecisionNode;
import com.tle.core.item.standard.operations.workflow.TaskOperation;

public class DecisionStatus extends SerialStatus
{
	private final ScriptEvaluator scriptEvaluator;

	public DecisionStatus(WorkflowNodeStatus bean, TaskOperation op, ScriptEvaluator scriptEvaluator)
	{
		super(bean, op);
		this.scriptEvaluator = scriptEvaluator;
	}

	@Override
	public void enter()
	{
		boolean bRet = true;

		DecisionNode decnode = (DecisionNode) node;

		// Evaluate Script, returns true if this step has been passed.
		boolean skipScript = op.overrideScript(this);
		if( !skipScript && !Check.isEmpty(decnode.getScript()) )
		{
			ScriptContext context = op.createScriptContext(null);
			try
			{
				Object result = scriptEvaluator.executeScript(decnode.getScript(), "decisionNode", //$NON-NLS-1$
					context, true);
				if( result instanceof Boolean )
				{
					bRet = ((Boolean) result).booleanValue();
				}
			}
			catch( ScriptException je )
			{
				throw new WorkflowException(je);
			}
		}
		else
		{
			bRet = skipScript;
		}

		// Do not enter if there are no children
		if( bRet && decnode.numberOfChildren() == 0 )
		{
			bRet = false;
		}

		if( bRet )
		{
			bean.setStatus(WorkflowNodeStatus.INCOMPLETE);
			update();
		}
		else
		{
			finished();
		}
	}
}
