package com.tle.core.item.standard.workflow.nodes;

import java.util.Set;

import org.apache.log4j.Logger;

import com.dytech.edge.common.ScriptContext;
import com.tle.beans.item.ItemTaskId;
import com.tle.common.Check;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.scripting.ScriptEvaluator;
import com.tle.common.scripting.ScriptException;
import com.tle.common.workflow.WorkflowNodeStatus;
import com.tle.common.workflow.node.ScriptNode;
import com.tle.common.workflow.node.WorkflowItem.MoveLive;
import com.tle.common.workflow.node.WorkflowNode;
import com.tle.core.item.standard.operations.workflow.TaskOperation;
import com.tle.core.notification.beans.Notification;

public class ScriptStatus extends AbstractNodeStatus
{
	private static final Logger LOGGER = Logger.getLogger(ScriptStatus.class);

	private final ScriptEvaluator scriptEvaluator;

	public ScriptStatus(WorkflowNodeStatus bean, TaskOperation op, ScriptEvaluator scriptEvaluator)
	{
		super(bean, op);
		this.scriptEvaluator = scriptEvaluator;
	}

	private WorkflowNode findNextNode()
	{
		boolean returnNext = false;
		for( WorkflowNode workflowNode : node.getParent().getChildren() )
		{
			if( returnNext )
			{
				return workflowNode;
			}

			if( workflowNode == node )
			{
				returnNext = true;
			}
		}
		return null;
	}

	@Override
	public boolean update()
	{
		if( bean.getStatus() == WorkflowNodeStatus.COMPLETE )
		{
			WorkflowNode next = findNextNode();
			if( next != null )
			{
				op.enter(next);
				return false;
			}
		}
		return true;
	}

	@Override
	public void enter()
	{
		bean.setStatus(WorkflowNodeStatus.INCOMPLETE);

		final ScriptNode scriptNode = (ScriptNode) node;
		if( scriptNode.getMovelive() == MoveLive.ARRIVAL )
		{
			op.makeLive(false);
		}

		boolean scriptError = false;
		if( !Check.isEmpty(scriptNode.getScript()) )
		{
			ScriptContext context = op.createScriptContext(null);
			try
			{
				scriptEvaluator.executeScript(scriptNode.getScript(), CurrentLocale.get(scriptNode.getName()), context,
					true);
			}
			catch( ScriptException je )
			{
				scriptError = true;
				op.createScriptErrorHistory(String.valueOf(node.getUuid()));
				if( scriptNode.isNotifyOnError() )
				{
					Set<String> userToNotify = op.getUsersToNotifyOnScriptError(scriptNode);
					op.addNotifications(getTaskKey(), userToNotify, Notification.REASON_SCRIPT_ERROR, false);
				}

				LOGGER.error("Script error at workflow script task : " + CurrentLocale.get(scriptNode.getName()), je);

				if( !scriptNode.isProceedNext() )
				{
					return;
				}
			}
		}

		if( scriptNode.getMovelive() == MoveLive.ACCEPTED )
		{
			op.makeLive(false);
		}
		if( !scriptError )
		{
			if( scriptNode.isNotifyOnCompletion() )
			{
				Set<String> userToNotify = op.getUsersToNotifyOnScriptCompletion(scriptNode);
				op.addNotifications(getTaskKey(), userToNotify, Notification.REASON_SCRIPT_EXECUTED, false);
			}
			op.createScriptCompleteHistory(String.valueOf(node.getUuid()));
		}

		bean.setStatus(WorkflowNodeStatus.COMPLETE);
		update();
	}

	@Override
	public boolean finished()
	{
		return super.finished();
	}

	private ItemTaskId getTaskKey()
	{
		return new ItemTaskId(op.getItem().getItemId(), node.getUuid());
	}
}
