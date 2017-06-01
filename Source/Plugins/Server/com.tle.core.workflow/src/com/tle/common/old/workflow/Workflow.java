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

package com.tle.common.old.workflow;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Lob;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Type;

import com.tle.beans.entity.BaseEntity;
import com.tle.common.Check;
import com.tle.common.old.workflow.node.WorkflowItem;
import com.tle.common.old.workflow.node.WorkflowNode;
import com.tle.common.old.workflow.node.WorkflowTreeNode;

@Entity
@AccessType("field")
public class Workflow extends BaseEntity
{
	private static final long serialVersionUID = 1L;

	private boolean movelive;

	@Type(type = "xstream_immutable")
	private WorkflowTreeNode root;

	@ElementCollection(fetch = FetchType.LAZY)
	@Fetch(value = FetchMode.SUBSELECT)
	@Column(name = "element")
	private Set<String> allGroups;

	public Workflow()
	{
		super();
	}

	@Lob
	public WorkflowTreeNode getRoot()
	{
		return root;
	}

	public void setRoot(WorkflowTreeNode root)
	{
		this.root = root;
	}

	public boolean isMovelive()
	{
		return movelive;
	}

	public void setMovelive(boolean movelive)
	{
		this.movelive = movelive;
	}

	public Set<String> getAllGroups()
	{
		return allGroups;
	}

	public void setAllGroups(Set<String> allGroups)
	{
		this.allGroups = allGroups;
	}

	public Map<String, WorkflowNode> getAllTasksAsMap()
	{
		return getTasksAsMap(new HashMap<String, WorkflowNode>(), getRoot());
	}

	private Map<String, WorkflowNode> getTasksAsMap(Map<String, WorkflowNode> map, WorkflowNode node)
	{
		map.put(node.getId(), node);
		if( !node.isLeafNode() )
		{
			WorkflowTreeNode treenode = (WorkflowTreeNode) node;
			int num = treenode.numberOfChildren();
			for( int i = 0; i < num; i++ )
			{
				getTasksAsMap(map, treenode.getChild(i));
			}
		}
		return map;
	}

	public Map<String, WorkflowItem> getAllWorkflowItems()
	{
		return getAllWorkflowItems(getRoot());
	}

	public void refreshGroupsList()
	{
		Set<String> groups = new HashSet<String>();
		for( WorkflowItem item : getAllWorkflowItems().values() )
		{
			List<String> gs = item.getGroups();
			if( !Check.isEmpty(gs) )
			{
				groups.addAll(gs);
			}
		}
		setAllGroups(groups);
	}

	public static Map<String, WorkflowItem> getAllWorkflowItems(WorkflowNode node)
	{
		return getAllWorkflowItems(node, null);
	}

	public static Map<String, WorkflowItem> getAllWorkflowItems(WorkflowNode node, String stopAtID)
	{
		// Use a LinkedHashMap to maintain correct step ordering
		Map<String, WorkflowItem> results = new LinkedHashMap<String, WorkflowItem>();
		recurseWorkflowItems(results, node, stopAtID);
		return results;
	}

	/**
	 * @return true if the recursion should stop.
	 */
	private static boolean recurseWorkflowItems(Map<String, WorkflowItem> items, WorkflowNode node, String stopAtID)
	{
		if( stopAtID != null && stopAtID.equals(node.getId()) )
		{
			return true;
		}

		if( node instanceof WorkflowItem )
		{
			items.put(node.getId(), (WorkflowItem) node);
		}

		if( !node.isLeafNode() )
		{
			WorkflowTreeNode treenode = (WorkflowTreeNode) node;
			int num = treenode.numberOfChildren();
			for( int i = 0; i < num; i++ )
			{
				boolean stop = recurseWorkflowItems(items, treenode.getChild(i), stopAtID);
				if( stop )
				{
					return true;
				}
			}
		}

		return false;
	}
}
