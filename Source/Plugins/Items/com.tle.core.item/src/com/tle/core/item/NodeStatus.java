package com.tle.core.item;

import com.tle.common.workflow.WorkflowNodeStatus;
import com.tle.common.workflow.node.WorkflowNode;

/**
 * @author Aaron
 *
 */
public interface NodeStatus
{
	boolean update();

	void enter();

	void clear();

	int getStatus();

	WorkflowNodeStatus getBean();

	WorkflowNode getWorkflowNode();

	void setWorkflowNode(WorkflowNode node);
}
