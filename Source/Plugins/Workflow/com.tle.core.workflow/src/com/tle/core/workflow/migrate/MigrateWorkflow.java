package com.tle.core.workflow.migrate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.tle.common.Check;
import com.tle.core.workflow.migrate.beans.node.FakeDecisionNode;
import com.tle.core.workflow.migrate.beans.node.FakeParallelNode;
import com.tle.core.workflow.migrate.beans.node.FakeSerialNode;
import com.tle.core.workflow.migrate.beans.node.FakeWorkflowItem;
import com.tle.core.workflow.migrate.beans.node.FakeWorkflowNode;

public class MigrateWorkflow
{
	public List<FakeWorkflowNode> convertNodes(com.tle.common.old.workflow.node.WorkflowTreeNode root)
	{
		List<FakeWorkflowNode> nodes = new ArrayList<FakeWorkflowNode>();
		convertOne(root, null, nodes);
		return nodes;
	}

	private void convertOne(com.tle.common.old.workflow.node.WorkflowNode node, FakeWorkflowNode parent,
		List<FakeWorkflowNode> nodes)
	{
		switch( node.getType() )
		{
			case com.tle.common.old.workflow.node.WorkflowNode.ITEM_TYPE:
				convertItem((com.tle.common.old.workflow.node.WorkflowItem) node, parent, nodes);
				break;
			case com.tle.common.old.workflow.node.WorkflowNode.SERIAL_TYPE:
				convertTree(new FakeSerialNode(), (com.tle.common.old.workflow.node.WorkflowTreeNode) node, parent, nodes);
				break;
			case com.tle.common.old.workflow.node.WorkflowNode.PARALLEL_TYPE:
				convertTree(new FakeParallelNode(), (com.tle.common.old.workflow.node.ParallelNode) node, parent, nodes);
				break;
			case com.tle.common.old.workflow.node.WorkflowNode.DECISION_TYPE:
				convertDecision((com.tle.common.old.workflow.node.DecisionNode) node, parent, nodes);
				break;

			default:
				// else it's the ROOT_TYPE
				break;
		}

	}

	private void convertDecision(com.tle.common.old.workflow.node.DecisionNode node, FakeWorkflowNode parent,
		List<FakeWorkflowNode> nodes)
	{
		FakeDecisionNode newNode = new FakeDecisionNode();
		newNode.setCollectionUuid(null);
		newNode.setScript(node.getScript());
		convertTree(newNode, node, parent, nodes);
	}

	private void convertTree(FakeWorkflowNode newNode, com.tle.common.old.workflow.node.WorkflowTreeNode node,
		FakeWorkflowNode parent, List<FakeWorkflowNode> nodes)
	{
		newNode.setRejectPoint(node.isRejectPoint());
		convertNode(newNode, node, parent, nodes);
		for( int i = 0; i < node.numberOfChildren(); i++ )
		{
			convertOne(node.getChild(i), newNode, nodes);
		}
	}

	private void convertItem(com.tle.common.old.workflow.node.WorkflowItem item, FakeWorkflowNode parent,
		List<FakeWorkflowNode> nodes)
	{
		FakeWorkflowItem newItem = new FakeWorkflowItem();
		newItem.setUsers(newSet(item.getUsers()));
		newItem.setGroups(newSet(item.getGroups()));
		newItem.setRoles(newSet(item.getRoles()));

		newItem.setDescription(item.getDescription());
		newItem.setUnanimousacceptance(item.isUnanimousacceptance());
		newItem.setEscalate(item.isEscalate());
		newItem.setEscalationdays(item.getEscalationdays());
		newItem.setMovelive(item.isMovelive());
		newItem.setRejectPoint(item.isRejectPoint());
		newItem.setAllowEditing(item.isAllowEditing());
		newItem.setAutoAssigns(item.getAutoAssigns());
		newItem.setAutoAssignNode(item.getAutoAssignFromMetadataPath());
		newItem.setAutoAssignSchemaUuid(item.getAutoAssignFromMetadataSchemaUuid());
		newItem.setUserPath(item.getUserPath());
		newItem.setUserSchemaUuid(item.getUserSchemaUuid());

		convertNode(newItem, item, parent, nodes);
	}

	private Set<String> newSet(Collection<String> orig)
	{
		Set<String> set = new HashSet<String>();
		if( orig != null )
		{
			set.addAll(orig);
		}
		return set;
	}

	private void convertNode(FakeWorkflowNode newNode, com.tle.common.old.workflow.node.WorkflowNode node,
		FakeWorkflowNode parent, List<FakeWorkflowNode> nodes)
	{
		String uuid = node.getId();
		if( Check.isEmpty(uuid) )
		{
			uuid = UUID.randomUUID().toString();
		}
		newNode.setUuid(uuid);
		newNode.setName(node.getName());
		newNode.setParent(parent);
		if( parent != null )
		{
			parent.addChild(newNode);
		}
		nodes.add(newNode);
	}

}
