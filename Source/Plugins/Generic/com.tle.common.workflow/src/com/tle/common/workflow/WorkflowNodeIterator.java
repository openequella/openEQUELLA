/*
 * Created on 27/03/2006
 */
package com.tle.common.workflow;

import java.util.Iterator;

import com.tle.common.workflow.node.DecisionNode;
import com.tle.common.workflow.node.ParallelNode;
import com.tle.common.workflow.node.SerialNode;
import com.tle.common.workflow.node.WorkflowItem;
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
					contains = process((WorkflowItem) n);
					break;
				case WorkflowNode.DECISION_TYPE:
					contains = process((DecisionNode) n);
					break;
				case WorkflowNode.PARALLEL_TYPE:
					contains = process((ParallelNode) n);
					break;
				case WorkflowNode.SERIAL_TYPE:
					contains = process((SerialNode) n);
					break;

				default:
					// Shouldn't be anything else
					break;
			}
		}
		return contains;
	}

	protected boolean process(WorkflowItem item)
	{
		return true;
	}

	protected boolean process(DecisionNode node)
	{
		return true;
	}

	protected boolean process(SerialNode node)
	{
		return true;
	}

	protected boolean process(ParallelNode node)
	{
		return true;
	}
}
