/*
 * Created on Aug 25, 2005
 */
package com.tle.core.workflow.nodes;

import com.tle.common.workflow.WorkflowNodeStatus;
import com.tle.common.workflow.node.ParallelNode;
import com.tle.common.workflow.node.WorkflowNode;
import com.tle.core.workflow.operations.tasks.TaskOperation;

public class ParallelStatus extends NodeStatus
{
	public ParallelStatus(WorkflowNodeStatus bean, TaskOperation op)
	{
		super(bean, op);
	}

	@Override
	public boolean update()
	{
		boolean updated = false;
		NodeStatus[] childStatuses = op.getChildStatuses(node);
		ParallelNode parnode = (ParallelNode) node;
		int num = parnode.numberOfChildren();
		for( int i = 0; i < num; i++ )
		{
			WorkflowNode child = parnode.getChild(i);
			if( childStatuses[i] == null )
			{
				updated = true;
				op.enter(child);
			}
		}
		if( allComplete(childStatuses) )
		{
			updated |= finished();
		}
		return updated;
	}

	@Override
	public void enter()
	{
		bean.setStatus(WorkflowNodeStatus.INCOMPLETE);
		ParallelNode parnode = (ParallelNode) node;
		int num = parnode.numberOfChildren();
		for( int i = 0; i < num; i++ )
		{
			WorkflowNode child = parnode.getChild(i);
			op.enter(child);
		}
	}
}
