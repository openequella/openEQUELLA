package com.tle.web.bulk.workflow.operations;

import javax.inject.Inject;

import com.dytech.edge.exceptions.WorkflowException;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.annotation.Nullable;
import com.tle.beans.item.HistoryEvent;
import com.tle.beans.item.HistoryEvent.Type;
import com.tle.beans.item.ModerationStatus;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.workflow.WorkflowNodeStatus;
import com.tle.common.workflow.node.ParallelNode;
import com.tle.common.workflow.node.WorkflowItem;
import com.tle.common.workflow.node.WorkflowNode;
import com.tle.core.item.standard.ItemOperationFactory;
import com.tle.core.item.standard.operations.workflow.TaskOperation;
import com.tle.core.item.standard.workflow.nodes.ScriptStatus;
import com.tle.core.item.standard.workflow.nodes.TaskStatus;
import com.tle.core.security.TLEAclManager;
import com.tle.exceptions.AccessDeniedException;

public class WorkflowMoveOperation extends TaskOperation
{
	private final String msg;
	private final String toStep;

	@Inject
	private ItemOperationFactory workflowFactory;
	@Inject
	private TLEAclManager aclService;

	@AssistedInject
	public WorkflowMoveOperation(@Assisted("msg") String msg, @Assisted("toStep") @Nullable String toStep)
	{
		this.msg = msg;
		this.toStep = toStep;
	}

	@Override
	public boolean execute()
	{
		if( !aclService.checkPrivilege("MANAGE_WORKFLOW", getWorkflow()) )
		{
			throw new AccessDeniedException(CurrentLocale.get("com.tle.core.services.item.error.nopriv",
				"MANAGE_WORKFLOW", getItemId()));
		}
		
		ModerationStatus status = getModerationStatus();
		status.setLastAction(params.getDateNow());

		int count = 0;
		boolean isMovingBack = false;
		for( WorkflowNodeStatus beanstatus : status.getStatuses() )
		{
			if( beanstatus.getNode().getType() == WorkflowNode.ITEM_TYPE
				&& beanstatus.getStatus() == WorkflowNodeStatus.INCOMPLETE )
			{
				TaskStatus task = (TaskStatus) getNodeStatus(beanstatus.getNode().getUuid());
				params.setCause(task.getBean());
				HistoryEvent move = createHistory(Type.taskMove);
				move.setComment(msg);
				setToStepFromTask(move, toStep);
				setStepFromTask(move, task.getId());
				WorkflowNode targetNode = task.getToStepByTaskId(toStep);

				if( targetNode == null )
				{
					throw new WorkflowException(
						CurrentLocale.get("com.tle.web.bulk.workflow.bulkop.movetask.error.nostep"));
				}

				if( isMovingToNewNode(targetNode) )
				{
					WorkflowNode parent = targetNode.getParent();
					if( parent != null && parent instanceof ParallelNode )
					{
						enter(parent, true);
					}
					else
					{
						enter(targetNode, true);
					}

					beanstatus.setStatus(WorkflowNodeStatus.COMPLETE);
					itemService.operation(getItemId(), workflowFactory.save());
				}
				else
				{
					if( count == 0 )
					{
						WorkflowNode parent = targetNode.getParent();
						if( parent != null && parent instanceof ParallelNode )
						{
							if( beanstatus.getNode().getParent() == parent )
							{
								continue;
							}

							enter(parent);
							beanstatus.setStatus(WorkflowNodeStatus.ARCHIVED);
							isMovingBack = true;
						}
						else
						{
							enter(targetNode);
							if( !task.isMovingTaskForward(beanstatus.getNode().getUuid(), toStep) )
							{
								beanstatus.setStatus(WorkflowNodeStatus.ARCHIVED);
							}
						}
					}
					else
					{
						if( isMovingBack || !task.isMovingTaskForward(beanstatus.getNode().getUuid(), toStep) )
						{
							beanstatus.setStatus(WorkflowNodeStatus.ARCHIVED);
						}
						else
						{
							beanstatus.setStatus(WorkflowNodeStatus.COMPLETE);
							itemService.operation(getItemId(), workflowFactory.save());
						}
					}
				}
				exitTask((WorkflowItem) beanstatus.getNode());
				count++;
			}
			else
				if( beanstatus.getNode().getType() == WorkflowNode.SCRIPT_TYPE
					&& beanstatus.getStatus() == WorkflowNodeStatus.INCOMPLETE )
			{
				ScriptStatus task = (ScriptStatus) getNodeStatus(beanstatus.getNode().getUuid());
				params.setCause(task.getBean());
				HistoryEvent move = createHistory(Type.taskMove);
				move.setComment(msg);
				setToStepFromTask(move, toStep);
				setStepFromTask(move, task.getId());
				WorkflowNode targetNode = task.getToStepByTaskId(toStep);
				reenter(targetNode);
			}
		}
		updateModeration();
		return true;
	}
}
