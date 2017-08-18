/*
 * Created on Aug 4, 2004
 */
package com.tle.core.item.standard.operations.workflow;

import com.tle.beans.item.Item;
import com.tle.common.workflow.Workflow;
import com.tle.common.workflow.WorkflowNodeStatus;
import com.tle.common.workflow.node.WorkflowNode;
import com.tle.common.workflow.node.WorkflowTreeNode;
import com.tle.core.guice.Bind;
import com.tle.core.item.NodeStatus;

/**
 * @author jmaginnis
 */
@Bind
public class CheckStepOperation extends TaskOperation
{
	protected CheckStepOperation()
	{
		// hide it
	}

	@Override
	public boolean execute()
	{
		Item item = getItem();
		// This is here for if the item is being purged
		if( item == null )
		{
			return false;
		}
		Workflow workflow = getWorkflow();
		if( workflow != null && getItem().isModerating() && checkAllTasks(workflow.getRoot()) )
		{
			updateModeration();
			return true;
		}
		return false;
	}

	private boolean checkAllTasks(WorkflowNode node)
	{
		boolean updated = false;
		NodeStatus nodeStatus = getNodeStatus(node.getUuid());
		if( nodeStatus != null )
		{
			if( nodeStatus.getStatus() == WorkflowNodeStatus.INCOMPLETE )
			{
				WorkflowNodeStatus bean = nodeStatus.getBean();
				char type = bean.getNode().getType();
				if( type == WorkflowNode.ITEM_TYPE || type == WorkflowNode.SCRIPT_TYPE)
				{
					updated |= update(node);
				}
				NodeStatus[] childStatuses = getChildStatuses(node);
				if( !node.isLeafNode() )
				{
					WorkflowTreeNode treenode = (WorkflowTreeNode) node;
					int num = treenode.numberOfChildren();
					for( int i = 0; i < num; i++ )
					{
						WorkflowNode child = treenode.getChild(i);
						if( type == WorkflowNode.PARALLEL_TYPE && childStatuses[i] == null )
						{
							updated |= update(node);
						}
						updated |= checkAllTasks(child);
					}
				}
			}
		}
		return updated;
	}

}
