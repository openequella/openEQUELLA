/*
 * Created on Aug 25, 2005
 */
package com.tle.core.workflow.nodes;

import com.dytech.edge.common.ScriptContext;
import com.dytech.edge.exceptions.WorkflowException;
import com.tle.common.Check;
import com.tle.common.scripting.ScriptEvaluator;
import com.tle.common.scripting.ScriptException;
import com.tle.common.workflow.WorkflowNodeStatus;
import com.tle.common.workflow.node.DecisionNode;
import com.tle.core.workflow.operations.tasks.TaskOperation;

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
