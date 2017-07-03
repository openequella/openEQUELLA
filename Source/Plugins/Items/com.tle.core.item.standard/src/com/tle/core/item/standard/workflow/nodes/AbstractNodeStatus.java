/*
 * Created on Aug 25, 2005
 */
package com.tle.core.item.standard.workflow.nodes;

import java.util.Collection;

import com.tle.common.workflow.Workflow;
import com.tle.common.workflow.WorkflowNodeStatus;
import com.tle.common.workflow.node.WorkflowNode;
import com.tle.core.item.NodeStatus;
import com.tle.core.item.standard.operations.workflow.TaskOperation;

public abstract class AbstractNodeStatus implements NodeStatus
{
	protected WorkflowNodeStatus bean;
	protected WorkflowNode node;
	protected TaskOperation op;

	public AbstractNodeStatus(WorkflowNodeStatus bean, TaskOperation op)
	{
		this.bean = bean;
		this.op = op;
	}

	public String getId()
	{
		return bean.getNode().getUuid();
	}

	@Override
	public void setWorkflowNode(WorkflowNode node)
	{
		this.node = node;
	}

	public boolean finished()
	{
		bean.setStatus(WorkflowNodeStatus.COMPLETE);
		op.update(node.getParent());
		return true;
	}

	protected boolean allComplete(NodeStatus[] childStatuses)
	{
		for( NodeStatus status : childStatuses )
		{
			if( status == null || status.getStatus() == WorkflowNodeStatus.INCOMPLETE )
			{
				return false;
			}
		}
		return true;
	}

	@Override
	public int getStatus()
	{
		return bean.getStatus();
	}

	public boolean isComplete()
	{
		return getStatus() == WorkflowNodeStatus.COMPLETE;
	}

	@Override
	public WorkflowNodeStatus getBean()
	{
		return bean;
	}

	public void setBean(WorkflowNodeStatus bean)
	{
		this.bean = bean;
	}

	@Override
	public WorkflowNode getWorkflowNode()
	{
		return node;
	}

	@Override
	public void clear()
	{
		// nothing
	}

	public WorkflowNode getToStepByTaskId(String taskid)
	{
		Workflow workflow = node.getWorkflow();
		Collection<WorkflowNode> values = workflow.getAllWorkflowTasks().values();
		for( WorkflowNode wnode : values )
		{
			if( wnode.getUuid().equals(taskid) )
			{
				return wnode;
			}
		}
		return null;
	}

	public boolean isMovingTaskForward(String currentStep, String toStep)
	{
		WorkflowNode currentNode = getToStepByTaskId(currentStep);
		WorkflowNode toNode = getToStepByTaskId(toStep);

		WorkflowNode parent1 = currentNode.getParent();
		WorkflowNode parent2 = toNode.getParent();

		int currentStepIndex = 0;
		int toStepIndex = 0;

		if( parent1 == parent2 )
		{
			currentStepIndex = currentNode.getChildIndex();
			toStepIndex = toNode.getChildIndex();
		}
		else
		{
			currentStepIndex = parent1.getChildIndex();
			toStepIndex = parent2.getChildIndex();
		}

		return currentStepIndex < toStepIndex;
	}
}
