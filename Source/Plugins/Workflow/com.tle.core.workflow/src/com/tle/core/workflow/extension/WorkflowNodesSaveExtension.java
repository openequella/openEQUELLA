package com.tle.core.workflow.extension;

import java.util.Set;

import com.tle.annotation.NonNullByDefault;
import com.tle.common.workflow.Workflow;
import com.tle.common.workflow.node.WorkflowNode;

/**
 * @author Aaron
 *
 */
@NonNullByDefault
public interface WorkflowNodesSaveExtension
{
	void workflowNodesSaved(Workflow oldWorkflow, Set<WorkflowNode> oldNodes, Set<WorkflowNode> deletedNodes,
		Set<WorkflowNode> changedNodes, Set<WorkflowNode> resaveNodes);
}
