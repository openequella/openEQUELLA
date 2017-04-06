package com.tle.web.api.item.resource.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.collect.Lists;
import com.tle.annotation.Nullable;
import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemStatus;
import com.tle.beans.item.ModerationStatus;
import com.tle.common.interfaces.equella.SimpleI18NStrings;
import com.tle.common.workflow.WorkflowNodeStatus;
import com.tle.common.workflow.node.WorkflowNode;
import com.tle.core.guice.Bind;
import com.tle.core.services.item.ItemService;
import com.tle.web.api.interfaces.beans.UserBean;
import com.tle.web.api.item.interfaces.ItemModerationResource;
import com.tle.web.api.item.interfaces.beans.ItemNodeStatusBean;
import com.tle.web.api.item.interfaces.beans.ItemNodeStatusBean.NodeStatus;
import com.tle.web.api.item.interfaces.beans.ItemNodeStatusBean.NodeType;
import com.tle.web.api.item.interfaces.beans.ItemNodeStatusMessageBean.MessageType;
import com.tle.web.api.item.interfaces.beans.ItemStatusBean;

/**
 * @author Aaron
 */
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
		return serializeStatus(item, null);
	}

	private ItemStatusBean serializeStatus(Item item, @Nullable Map<String, WorkflowNode> nodeMapOpt)
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
			List<ItemNodeStatusBean> serializedNodes = serializeNodeStatus(nodes, nodeMapOpt);
			if( serializedNodes.size() > 0 )
			{
				statusBean.setNodes(serializedNodes.get(0));
			}
		}
		return statusBean;
	}

	private List<ItemNodeStatusBean> serializeNodeStatus(Set<WorkflowNodeStatus> nodes,
		@Nullable Map<String, WorkflowNode> nodeMapOpt)
	{
		final List<ItemNodeStatusBean> isnbs = Lists.newArrayList();
		for( WorkflowNodeStatus node : nodes )
		{
			isnbs.add(serializeNodeStatus(node, nodeMapOpt));
		}
		return isnbs;
	}

	private ItemNodeStatusBean serializeNodeStatus(WorkflowNodeStatus status, Map<String, WorkflowNode> nodeMap)
	{
		WorkflowNode node = status.getNode();
		char nodeStatusChar = status.getStatus();
		String uuid = node.getUuid();

		NodeStatus statusEnum = NODESTATUS_MAP.get(nodeStatusChar);

		ItemNodeStatusBean statusBean = new ItemNodeStatusBean(uuid);
		// ItemNodeStatusBean statusBean = (nodeStatusChar, node) match {
		// case (Some(taskStatus: WorkflowItemStatus), task: WorkflowItem) => {
		// val taskStatusBean = new ItemTaskStatusBean(uuid)
		// taskStatusBean.setOverdue(taskStatus.overdue)
		// taskStatusBean.setDescription(new
		// SimpleI18NStrings(task.description).asI18NString(null))
		// taskStatusBean.setDue(if (taskStatus.dateDue.isDefined)
		// taskStatus.dateDue.get else null)
		// taskStatusBean.setStarted(taskStatus.started)
		// taskStatusBean.setPriority(task.priority)
		// taskStatus.cause.flatMap(ns => nodeMap.get(ns.nodeUuid)).foreach {
		// causeNode =>
		// val cause = new ItemNodeStatusBean(causeNode.id.toString)
		// addMessages(cause, taskStatus.cause.get.comments)
		// taskStatusBean.setCause(cause)
		// }
		// taskStatus.assignedTo.foreach { at =>
		// taskStatusBean.setAssignedTo(new UserBean(at)) }
		// taskStatusBean.setAcceptedUsers(taskStatus.acceptedUsers.toList)
		// taskStatusBean
		// }
		// case _ => new ItemNodeStatusBean(uuid)
		// }
		statusBean.setStatus(statusEnum);
		statusBean.setType(NODETYPE_MAP.get(node.getType()));
		LanguageBundle name = node.getName();
		if( name != null )
		{
			statusBean.setName(new SimpleI18NStrings(name.getStrings()).asI18NString(null));
		}
		// addMessages(statusBean, status.messages)

		// val childBeans = status.children.map(serializeNodeStatus(_, nodeMap))
		// childBeans.headOption.foreach { _ =>
		// statusBean.setChildren(childBeans) }

		return statusBean;
	}
	// private void addMessages(ItemNodeStatusBean
	// statusBean,Collection<WorkflowMessage> messages)
	// {
	// val msgs = messages.map(workflowMessage => new
	// ItemNodeStatusMessageBean(MSGTYPE_MAP(workflowMessage.typeChar), new
	// UserBean(workflowMessage.user),
	// workflowMessage.message, workflowMessage.date))
	// msgs.headOption.foreach { _ => statusBean.setComments(msgs.toList) }
	// }
}
