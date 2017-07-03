/*
 * Created on 27/03/2006
 */
package com.tle.common.workflow;

import java.util.Iterator;

import com.tle.common.workflow.node.WorkflowNode;

public class WorkflowNodeIterator
{
	public WorkflowNodeIterator()
	{
		// Do nothing
	}

	public boolean iterate(Workflow workflow)
	{
		return iterate(workflow.getRoot());
	}

	public boolean iterate(WorkflowNode node)
	{
		boolean contains = false;
		for( Iterator<WorkflowNode> i = node.iterateChildren(); !contains && i.hasNext(); )
		{
			WorkflowNode n = i.next();
			if( n.canAddChildren() )
			{
				contains = iterate(n);
			}
			switch( n.getType() )
			{
				case WorkflowNode.ITEM_TYPE:
					contains = true;
					break;
				case WorkflowNode.DECISION_TYPE:
					contains = true;
					break;
				case WorkflowNode.PARALLEL_TYPE:
					contains = true;
					break;
				case WorkflowNode.SERIAL_TYPE:
					contains = true;
					break;
				case WorkflowNode.SCRIPT_TYPE:
					contains = true;
					break;

				default:
					// Shouldn't be anything else
					break;
			}
		}
		return contains;
	}
}
