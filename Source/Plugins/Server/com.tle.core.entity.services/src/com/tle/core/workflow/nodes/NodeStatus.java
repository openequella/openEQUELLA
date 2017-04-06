/*
 * Created on Aug 25, 2005
 */
package com.tle.core.workflow.nodes;

import com.tle.common.workflow.WorkflowNodeStatus;
import com.tle.common.workflow.node.WorkflowNode;
import com.tle.core.workflow.operations.tasks.TaskOperation;

public abstract class NodeStatus
{
	protected WorkflowNodeStatus bean;
	protected WorkflowNode node;
	protected TaskOperation op;

	public NodeStatus(WorkflowNodeStatus bean, TaskOperation op)
	{
		this.bean = bean;
		this.op = op;
	}

	public String getId()
	{
		return bean.getNode().getUuid();
	}

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

	public int getStatus()
	{
		return bean.getStatus();
	}

	public boolean isComplete()
	{
		return getStatus() == WorkflowNodeStatus.COMPLETE;
	}

	public WorkflowNodeStatus getBean()
	{
		return bean;
	}

	public void setBean(WorkflowNodeStatus bean)
	{
		this.bean = bean;
	}

	public abstract boolean update();

	public abstract void enter();

	public WorkflowNode getWorkflowNode()
	{
		return node;
	}

	public void clear()
	{
		// nothing
	}
}
