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

package com.tle.web.api.item.resource.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemStatus;
import com.tle.beans.item.ModerationStatus;
import com.tle.common.interfaces.equella.SimpleI18NStrings;
import com.tle.common.workflow.WorkflowItemStatus;
import com.tle.common.workflow.WorkflowMessage;
import com.tle.common.workflow.WorkflowNodeStatus;
import com.tle.common.workflow.node.WorkflowItem;
import com.tle.common.workflow.node.WorkflowNode;
import com.tle.core.guice.Bind;
import com.tle.core.item.service.ItemService;
import com.tle.web.api.interfaces.beans.UserBean;
import com.tle.web.api.item.interfaces.ItemModerationResource;
import com.tle.web.api.item.interfaces.beans.ItemNodeStatusBean;
import com.tle.web.api.item.interfaces.beans.ItemNodeStatusBean.NodeStatus;
import com.tle.web.api.item.interfaces.beans.ItemNodeStatusBean.NodeType;
import com.tle.web.api.item.interfaces.beans.ItemNodeStatusMessageBean;
import com.tle.web.api.item.interfaces.beans.ItemNodeStatusMessageBean.MessageType;
import com.tle.web.api.item.interfaces.beans.ItemStatusBean;
import com.tle.web.api.item.interfaces.beans.ItemTaskStatusBean;

/**
 * @author Aaron
 */
@NonNullByDefault
@Bind(ItemModerationResource.class)
@Singleton
public class ItemModerationResourceImpl implements ItemModerationResource
{
	private static final Map<Character, NodeType> NODETYPE_MAP = new HashMap<>();

	static
	{
		NODETYPE_MAP.put('t', NodeType.task);
		NODETYPE_MAP.put('d', NodeType.decision);
		NODETYPE_MAP.put('s', NodeType.serial);
		NODETYPE_MAP.put('p', NodeType.parallel);
	}

	private static final Map<Character, MessageType> MSGTYPE_MAP = new HashMap<>();

	static
	{
		MSGTYPE_MAP.put('a', MessageType.accept);
		MSGTYPE_MAP.put('r', MessageType.reject);
		MSGTYPE_MAP.put('s', MessageType.submit);
		MSGTYPE_MAP.put('c', MessageType.comment);
	}

	private static final Map<Character, NodeStatus> NODESTATUS_MAP = new HashMap<>();

	static
	{
		NODESTATUS_MAP.put('i', NodeStatus.incomplete);
		NODESTATUS_MAP.put('c', NodeStatus.complete);
	}

	@Inject
	private ItemService itemService;

	@Override
	public ItemStatusBean getModeration(String uuid, int version)
	{
		Item item = itemService.get(new ItemId(uuid, version));
		WorkflowNode rootWorkflowNode = item.getItemDefinition().getWorkflow().getRoot();
		return serializeStatus(item, rootWorkflowNode);
	}

	private ItemStatusBean serializeStatus(Item item, WorkflowNode rootWorkflowNode)
	{
		ItemStatusBean statusBean = new ItemStatusBean();

		ItemStatus itemStatus = item.getStatus();
		if( itemStatus == ItemStatus.REJECTED )
		{
			ModerationStatus moderation = item.getModeration();
			statusBean.setRejectedMessage(moderation.getRejectedMessage());
			statusBean.setRejectedBy(new UserBean(moderation.getRejectedBy()));
		}
		statusBean.setStatus(itemStatus.toString().toLowerCase());
		ModerationStatus modStatus = item.getModeration();
		Set<WorkflowNodeStatus> nodes = modStatus.getStatuses();
		if( nodes != null )
		{
			statusBean.setNodes(serializeNodeStatus(nodes, rootWorkflowNode));
		}
		return statusBean;
	}

	private void convertTree(Map<String, TreeNode> treeNodeMap, TreeNode treeNode, WorkflowNode workflowNode)
	{
		for( WorkflowNode childWorkflowNode : workflowNode.getChildren() )
		{
			final TreeNode childTreeNode = treeNodeMap.get(childWorkflowNode.getUuid());
			if( childTreeNode != null )
			{
				treeNode.addChild(childTreeNode);
				convertTree(treeNodeMap, childTreeNode, childWorkflowNode);
			}
		}
	}

	@Nullable
	private ItemNodeStatusBean serializeNodeStatus(Set<WorkflowNodeStatus> nodeStatuses, WorkflowNode rootWorkflowNode)
	{
		final Map<String, TreeNode> treeNodeMap = new HashMap<>();
		for( WorkflowNodeStatus ns : nodeStatuses )
		{
			if( ns.getStatus() != WorkflowNodeStatus.ARCHIVED )
			{
				treeNodeMap.put(ns.getNode().getUuid(), new TreeNode(ns));
			}
		}

		TreeNode root = treeNodeMap.get(rootWorkflowNode.getUuid());
		if( root != null )
		{
			convertTree(treeNodeMap, root, rootWorkflowNode);

			return serializeNodeStatus(root);
		}
		return null;
	}

	private ItemNodeStatusBean serializeNodeStatus(TreeNode treeNode)
	{
		WorkflowNodeStatus status = treeNode.getValue();
		WorkflowNode node = status.getNode();
		char nodeStatusChar = status.getStatus();
		String uuid = node.getUuid();

		NodeStatus statusEnum = NODESTATUS_MAP.get(nodeStatusChar);

		ItemNodeStatusBean statusBean;
		if( node.getType() == WorkflowNode.ITEM_TYPE )
		{
			WorkflowItem task = (WorkflowItem) node;
			WorkflowItemStatus taskStatus = (WorkflowItemStatus) status;
			ItemTaskStatusBean taskStatusBean = new ItemTaskStatusBean(uuid);
			statusBean = taskStatusBean;

			taskStatusBean.setOverdue(taskStatus.isOverdue());
			if( task.getDescription() != null )
			{
				taskStatusBean
					.setDescription(new SimpleI18NStrings(task.getDescription().getStrings()).asI18NString(null));
			}
			taskStatusBean.setDue(taskStatus.getDateDue());
			taskStatusBean.setStarted(taskStatus.getStarted());
			taskStatusBean.setPriority(task.getPriority());
			// taskStatus.cause.flatMap(ns => nodeMap.get(ns.nodeUuid)).foreach {
			// causeNode =>
			// val cause = new ItemNodeStatusBean(causeNode.id.toString)
			// addMessages(cause, taskStatus.cause.get.comments)
			// taskStatusBean.setCause(cause)
			// }
			taskStatusBean.setAssignedTo(new UserBean(taskStatus.getAssignedTo()));
			taskStatusBean.setAcceptedUsers(new ArrayList<String>(taskStatus.getAcceptedUsers()));
		}
		else
		{
			statusBean = new ItemNodeStatusBean(uuid);
		}

		statusBean.setStatus(statusEnum);
		statusBean.setType(NODETYPE_MAP.get(node.getType()));
		LanguageBundle name = node.getName();
		if( name != null )
		{
			statusBean.setName(new SimpleI18NStrings(name.getStrings()).asI18NString(null));
		}
		addMessages(statusBean, new ArrayList<>(status.getComments()));

		List<ItemNodeStatusBean> childBeans = Lists.transform(treeNode.getChildren(),
			new Function<TreeNode, ItemNodeStatusBean>()
			{
				@Override
				public ItemNodeStatusBean apply(TreeNode input)
				{
					return serializeNodeStatus(input);
				}
			});
		statusBean.setChildren(childBeans);

		return statusBean;
	}

	private void addMessages(ItemNodeStatusBean statusBean, List<WorkflowMessage> messages)
	{
		statusBean.setComments(
			new ArrayList<>(Lists.transform(messages, new Function<WorkflowMessage, ItemNodeStatusMessageBean>()
			{
				@Override
				public ItemNodeStatusMessageBean apply(WorkflowMessage msg)
				{
					return new ItemNodeStatusMessageBean(MSGTYPE_MAP.get(msg.getType()), new UserBean(msg.getUser()),
						msg.getMessage(), msg.getDate());
				}
			})));
	}

	private static class TreeNode
	{
		private WorkflowNodeStatus nodeValue;
		private List<TreeNode> children;

		public TreeNode(WorkflowNodeStatus nodeValue)
		{
			this.nodeValue = nodeValue;
			children = new ArrayList<TreeNode>();
		}

		public boolean addChild(TreeNode n)
		{
			return children.add(n);
		}

		public List<TreeNode> getChildren()
		{
			return children;
		}

		public WorkflowNodeStatus getValue()
		{
			return nodeValue;
		}
	}
}
