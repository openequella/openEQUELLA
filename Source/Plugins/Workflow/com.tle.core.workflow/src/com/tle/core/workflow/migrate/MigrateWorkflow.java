/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.core.workflow.migrate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.tle.common.Check;
import com.tle.core.workflow.migrate.beans.node.DecisionNode;
import com.tle.core.workflow.migrate.beans.node.ParallelNode;
import com.tle.core.workflow.migrate.beans.node.SerialNode;
import com.tle.core.workflow.migrate.beans.node.WorkflowItem;
import com.tle.core.workflow.migrate.beans.node.WorkflowNode;

public class MigrateWorkflow
{
	public List<WorkflowNode> convertNodes(com.tle.common.old.workflow.node.WorkflowTreeNode root)
	{
		List<WorkflowNode> nodes = new ArrayList<WorkflowNode>();
		convertOne(root, null, nodes);
		return nodes;
	}

	private void convertOne(com.tle.common.old.workflow.node.WorkflowNode node, WorkflowNode parent,
		List<WorkflowNode> nodes)
	{
		switch( node.getType() )
		{
			case com.tle.common.old.workflow.node.WorkflowNode.ITEM_TYPE:
				convertItem((com.tle.common.old.workflow.node.WorkflowItem) node, parent, nodes);
				break;
			case com.tle.common.old.workflow.node.WorkflowNode.SERIAL_TYPE:
				convertTree(new SerialNode(), (com.tle.common.old.workflow.node.WorkflowTreeNode) node, parent, nodes);
				break;
			case com.tle.common.old.workflow.node.WorkflowNode.PARALLEL_TYPE:
				convertTree(new ParallelNode(), (com.tle.common.old.workflow.node.ParallelNode) node, parent, nodes);
				break;
			case com.tle.common.old.workflow.node.WorkflowNode.DECISION_TYPE:
				convertDecision((com.tle.common.old.workflow.node.DecisionNode) node, parent, nodes);
				break;

			default:
				// else it's the ROOT_TYPE
				break;
		}

	}

	private void convertDecision(com.tle.common.old.workflow.node.DecisionNode node, WorkflowNode parent,
		List<WorkflowNode> nodes)
	{
		DecisionNode newNode = new DecisionNode();
		newNode.setCollectionUuid(null);
		newNode.setScript(node.getScript());
		convertTree(newNode, node, parent, nodes);
	}

	private void convertTree(WorkflowNode newNode, com.tle.common.old.workflow.node.WorkflowTreeNode node,
							 WorkflowNode parent, List<WorkflowNode> nodes)
	{
		newNode.setRejectPoint(node.isRejectPoint());
		convertNode(newNode, node, parent, nodes);
		for( int i = 0; i < node.numberOfChildren(); i++ )
		{
			convertOne(node.getChild(i), newNode, nodes);
		}
	}

	private void convertItem(com.tle.common.old.workflow.node.WorkflowItem item, WorkflowNode parent,
		List<WorkflowNode> nodes)
	{
		WorkflowItem newItem = new WorkflowItem();
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

	private void convertNode(WorkflowNode newNode, com.tle.common.old.workflow.node.WorkflowNode node,
							 WorkflowNode parent, List<WorkflowNode> nodes)
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
