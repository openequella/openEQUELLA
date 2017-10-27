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

package com.tle.common.workflow;

import com.google.common.base.Function;
import com.tle.beans.entity.BaseEntity;
import com.tle.common.workflow.node.WorkflowItem;
import com.tle.common.workflow.node.WorkflowNode;
import com.tle.common.workflow.node.WorkflowTreeNode;
import org.hibernate.annotations.AccessType;

import javax.persistence.*;
import java.util.*;

@Entity
@AccessType("field")
public class Workflow extends BaseEntity
{
	private static final long serialVersionUID = 1L;

	private boolean movelive;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "workflow_id", nullable = false)
	private Set<WorkflowNode> nodes;
	@Transient
	private WorkflowNode rootCache;

	public Workflow()
	{
		super();
	}

	public boolean isMovelive()
	{
		return movelive;
	}

	public void setMovelive(boolean movelive)
	{
		this.movelive = movelive;
	}

	public synchronized Set<WorkflowNode> getNodes()
	{
		return nodes;
	}

	public synchronized void setNodes(Set<WorkflowNode> nodes)
	{
		this.nodes = nodes;
	}

	public synchronized WorkflowNode getRoot()
	{
		if( rootCache == null && nodes != null )
		{
			for( WorkflowNode node : nodes )
			{
				WorkflowNode parent = node.getParent();
				if( parent == null )
				{
					rootCache = node;
				}
				else
				{
					parent.setChild(node);
				}
			}
		}
		return rootCache;
	}

	public Map<String, WorkflowNode> getAllNodesAsMap()
	{
		Map<String, WorkflowNode> allNodes = new HashMap<String, WorkflowNode>();
		for( WorkflowNode node : nodes )
		{
			allNodes.put(node.getUuid(), node);
		}
		return allNodes;
	}

	public Map<String, WorkflowItem> getAllWorkflowItems()
	{
		Map<String, WorkflowItem> allNodes = new HashMap<String, WorkflowItem>();
		for( WorkflowNode node : nodes )
		{
			if( node.getType() == WorkflowNode.ITEM_TYPE )
			{
				allNodes.put(node.getUuid(), (WorkflowItem) node);
			}
		}
		return allNodes;
	}

	public Map<String, WorkflowNode> getAllWorkflowTasks()
	{
		Map<String, WorkflowNode> allNodes = new HashMap<String, WorkflowNode>();
		for( WorkflowNode node : nodes )
		{
			if( node.getType() == WorkflowNode.ITEM_TYPE || node.getType() == WorkflowNode.SCRIPT_TYPE )
			{
				allNodes.put(node.getUuid(), node);
			}
		}
		return allNodes;
	}

	public static Map<String, WorkflowNode> getAllWorkflowTasks(WorkflowNode node)
	{
		// Use a LinkedHashMap to maintain correct step ordering
		Map<String, WorkflowNode> results = new LinkedHashMap<>();
		recurseWorkflowItems(results, node, new Function<WorkflowNode, Boolean>(){
			@Override
			public Boolean apply(WorkflowNode node) {
				return node.getType() == WorkflowNode.ITEM_TYPE || node.getType() == WorkflowNode.SCRIPT_TYPE;
			}
		});
		return results;
	}

	public static Map<String, WorkflowItem> getAllWorkflowItems(WorkflowNode node)
	{
		// Use a LinkedHashMap to maintain correct step ordering
		Map<String, WorkflowItem> results = new LinkedHashMap<String, WorkflowItem>();
		recurseWorkflowItems(results, node, new Function<WorkflowNode, Boolean>(){
			@Override
			public Boolean apply(WorkflowNode node) {
				return node instanceof WorkflowItem;
			}
		});
		return results;
	}

	/**
	 * @return true if the recursion should stop.
	 */
	private static <T extends WorkflowNode> boolean  recurseWorkflowItems(Map<String, T> items, WorkflowNode node, Function<WorkflowNode, Boolean> includeFunc)
	{
		if( includeFunc.apply(node) )
		{
			items.put(node.getUuid(), (T) node);
		}

		if( !node.isLeafNode() )
		{
			WorkflowTreeNode treenode = (WorkflowTreeNode) node;
			int num = treenode.numberOfChildren();
			for( int i = 0; i < num; i++ )
			{
				boolean stop = recurseWorkflowItems(items, treenode.getChild(i), includeFunc);
				if( stop )
				{
					return true;
				}
			}
		}

		return false;
	}

	public synchronized void setRoot(WorkflowTreeNode root)
	{
		nodes = new HashSet<WorkflowNode>();
		addToSet(root);
		this.rootCache = root;
	}

	private void addToSet(WorkflowNode node)
	{
		nodes.add(node);
		for( WorkflowNode child : node.getChildren() )
		{
			addToSet(child);
		}
	}

	@Override
	public String toString()
	{
		final StringBuilder string = new StringBuilder(super.toString());
		try
		{
			dfsWorkflowItems(getRoot(), 0, new NodeVisitorCallback()
			{
				@Override
				public void visitNode(WorkflowNode node, int depth)
				{
					string.append('\n');
					for( int i = 0; i < depth; i++ )
					{
						string.append(' ');
						string.append(' ');
					}
					string.append('-');
					string.append(node.getName());
				}
			});
		}
		catch( Exception e )
		{
			// Forget about it
		}
		return string.toString();
	}

	private interface NodeVisitorCallback
	{
		void visitNode(WorkflowNode node, int depth);
	}

	private static void dfsWorkflowItems(WorkflowNode node, int depth, NodeVisitorCallback cb)
	{
		cb.visitNode(node, depth);
		if( !node.isLeafNode() )
		{
			WorkflowTreeNode treenode = (WorkflowTreeNode) node;
			int num = treenode.numberOfChildren();
			for( int i = 0; i < num; i++ )
			{
				dfsWorkflowItems(treenode.getChild(i), depth + 1, cb);
			}
		}
	}
}
