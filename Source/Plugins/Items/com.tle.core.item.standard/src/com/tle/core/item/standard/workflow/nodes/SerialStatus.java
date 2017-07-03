/*
 * Created on Aug 25, 2005
 */
package com.tle.core.item.standard.workflow.nodes;

import com.tle.common.workflow.WorkflowNodeStatus;
import com.tle.common.workflow.node.WorkflowNode;
import com.tle.common.workflow.node.WorkflowTreeNode;
import com.tle.core.item.NodeStatus;
import com.tle.core.item.standard.operations.workflow.TaskOperation;

public class SerialStatus extends AbstractNodeStatus
{
	public SerialStatus(WorkflowNodeStatus bean, TaskOperation op)
	{
		super(bean, op);
	}

	@Override
	public boolean update()
	{
		WorkflowTreeNode treenode = (WorkflowTreeNode) node;
		int num = treenode.numberOfChildren();
		for( int i = 0; i < num; i++ )
		{
			WorkflowNode child = treenode.getChild(i);
			NodeStatus childStatus = op.getNodeStatus(child.getUuid());
			if( childStatus == null )
			{
				op.enter(child);
				return false;
			}

			if( childStatus.getStatus() == WorkflowNodeStatus.INCOMPLETE )
			{
				return false;
			}
		}
		return finished();
	}

	@Override
	public void enter()
	{
		bean.setStatus(WorkflowNodeStatus.INCOMPLETE);
		update();
	}
}
