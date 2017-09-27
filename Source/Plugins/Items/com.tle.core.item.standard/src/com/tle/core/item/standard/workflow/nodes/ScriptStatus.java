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
	private ScriptNode scriptNode;

	public ScriptStatus(WorkflowNodeStatus bean, TaskOperation op, ScriptEvaluator scriptEvaluator)
	{
		super(bean, op);
		this.scriptEvaluator = scriptEvaluator;
	}

	@Override
	public void setWorkflowNode(WorkflowNode node)
	{
		super.setWorkflowNode(node);
		this.scriptNode = (ScriptNode) node;
	}

	public boolean update()
	{
		ScriptException se = null;
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
				se = je;
			}
		}
		if (se != null)
		{
			op.createScriptErrorHistory(String.valueOf(node.getUuid()));
			Set<String> userToNotify = op.getUsersToNotifyOnScriptError(scriptNode);
			op.addNotifications(getTaskKey(), userToNotify, Notification.REASON_SCRIPT_ERROR, false);

			LOGGER.error("Script error at workflow script task : " + CurrentLocale.get(scriptNode.getName()), se);
			if( scriptNode.isProceedNext() )
			{
				return finished();
			}
			return false;
		}
		else
		{
			if( scriptNode.isNotifyOnCompletion() )
			{
				Set<String> userToNotify = op.getUsersToNotifyOnScriptCompletion(scriptNode);
				op.addNotifications(getTaskKey(), userToNotify, Notification.REASON_SCRIPT_EXECUTED, false);
			}
			op.removeNotificationsForKey(getTaskKey(), Notification.REASON_SCRIPT_ERROR);
			op.createScriptCompleteHistory(String.valueOf(node.getUuid()));
			return finished();
		}
	}

	@Override
	public boolean finished()
	{
		if( scriptNode.getMovelive() == MoveLive.ACCEPTED )
		{
			op.makeLive(false);
		}
		return super.finished();
	}

	@Override
	public void enter()
	{
		bean.setStatus(WorkflowNodeStatus.INCOMPLETE);
		if( scriptNode.getMovelive() == MoveLive.ARRIVAL )
		{
			op.makeLive(false);
		}
		update();
	}

	private ItemTaskId getTaskKey()
	{
		return new ItemTaskId(op.getItem().getItemId(), node.getUuid());
	}
}
