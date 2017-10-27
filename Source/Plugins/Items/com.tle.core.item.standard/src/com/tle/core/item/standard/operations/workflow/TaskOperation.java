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

package com.tle.core.item.standard.operations.workflow;

import java.util.*;

import javax.inject.Inject;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.exceptions.WorkflowException;
import com.google.common.collect.Multimap;
import com.tle.beans.item.HistoryEvent;
import com.tle.beans.item.HistoryEvent.Type;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.ModerationStatus;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.common.workflow.Workflow;
import com.tle.common.workflow.WorkflowItemStatus;
import com.tle.common.workflow.WorkflowMessage;
import com.tle.common.workflow.WorkflowNodeStatus;
import com.tle.common.workflow.node.ScriptNode;
import com.tle.common.workflow.node.WorkflowItem;
import com.tle.common.workflow.node.WorkflowNode;
import com.tle.common.workflow.node.WorkflowTreeNode;
import com.tle.core.item.NodeStatus;
import com.tle.core.item.operations.ItemOperationParams;
import com.tle.core.item.standard.operations.AbstractStandardWorkflowOperation;
import com.tle.core.item.standard.workflow.nodes.DecisionStatus;
import com.tle.core.item.standard.workflow.nodes.ParallelStatus;
import com.tle.core.item.standard.workflow.nodes.ScriptStatus;
import com.tle.core.item.standard.workflow.nodes.SerialStatus;
import com.tle.core.item.standard.workflow.nodes.TaskStatus;
import com.tle.core.notification.beans.Notification;
import com.tle.core.notification.standard.service.NotificationPreferencesService;
import com.tle.core.workflow.service.TaskStatisticsService;
import com.tle.core.workflow.service.WorkflowService;

public abstract class TaskOperation extends AbstractStandardWorkflowOperation
{
	@Inject
	protected WorkflowService workflowService;
	@Inject
	protected TaskStatisticsService taskStatsService;
	@Inject
	private NotificationPreferencesService notificationPrefs;

	public boolean update(WorkflowNode node)
	{
		if( node != null )
		{
			NodeStatus nodestat = getNodeStatus(node.getUuid());
			if( nodestat != null )
			{
				return nodestat.update();
			}
		}
		return false;
	}

	public void enter(WorkflowNode node)
	{
		enter(node, false);
	}

	public void enter(WorkflowNode node, boolean changeCause)
	{
		WorkflowNodeStatus statbean;
		int type = node.getType();
		switch( type )
		{
			case WorkflowNode.ITEM_TYPE:
				statbean = new WorkflowItemStatus(node, params.getCause());
				break;
			default:
				statbean = new WorkflowNodeStatus(node);
				break;
		}
		statbean.setModStatus(getModerationStatus());
		if( changeCause )
		{
			params.setCause(statbean);
		}
		NodeStatus nodestatus = addWrappedStatus(getStatusMap(), statbean);
		nodestatus.setWorkflowNode(node);
		nodestatus.enter();
	}

	public void reenter(WorkflowNode node)
	{
		clearStatuses(node);
		enter(node);
	}

	public void createScriptCompleteHistory(String id)
	{
		HistoryEvent history = createHistory(Type.scriptComplete);
		setStepFromTask(history, id);
	}

	public void createScriptErrorHistory(String id)
	{
		HistoryEvent history = createHistory(Type.scriptError);
		setStepFromTask(history, id);
	}

	public void setToStepFromTask(HistoryEvent event, String tostep)
	{
		try
		{
			NodeStatus nodeStatus = getNodeStatus(tostep);
			if( nodeStatus != null )
			{
				event.setToStepName(CurrentLocale.get(nodeStatus.getWorkflowNode().getName()));
			}
		}
		catch( WorkflowException e )
		{
			LOGGER.error("Error getting node status: " + tostep, e); //$NON-NLS-1$
		}
		event.setToStep(tostep);
	}

	protected void setStepFromTask(HistoryEvent event, String id)
	{
		try
		{
			NodeStatus nodeStatus = getNodeStatus(id);
			if( nodeStatus != null )
			{
				event.setStepName(CurrentLocale.get(nodeStatus.getWorkflowNode().getName()));
			}
		}
		catch( WorkflowException e )
		{
			LOGGER.error("Error getting node status: " + id, e); //$NON-NLS-1$
		}
		event.setStep(id);
	}

	public void clearAllStatuses()
	{
		removeModerationNotifications();
		ModerationStatus status = getModerationStatus();
		status.setNeedsReset(false);
		status.getStatuses().clear();
		params.clearAllStatuses();
		exitTasksForItem();
	}

	public void resetWorkflow()
	{
		Workflow workflow = getWorkflow();
		clearAllStatuses();
		ModerationStatus status = getModerationStatus();
		if( workflow != null )
		{
			HistoryEvent history = createHistory(Type.resetworkflow);
			status.setStart(new Date());
			if( workflow.isMovelive() )
			{
				makeLive(false);
			}
			getItem().setModerating(true);
			enter(workflow.getRoot(), true);
			setStepFromTask(history, workflow.getRoot().getUuid());
		}
		else
		{
			makeLive(true);
		}
	}

	public boolean isMovingToNewNode(WorkflowNode node)
	{
		String id = node.getUuid();
		Map<String, NodeStatus> statusMap = getStatusMap();
		NodeStatus stat = statusMap.get(id);
		if( stat == null )
		{
			return true;
		}
		return false;
	}

	private void clearStatuses(WorkflowNode node)
	{
		String id = node.getUuid();
		Map<String, NodeStatus> statusMap = getStatusMap();
		NodeStatus stat = statusMap.get(id);
		if( stat != null )
		{
			statusMap.remove(id);
			stat.clear();
			if( !node.isLeafNode() )
			{
				WorkflowTreeNode treenode = (WorkflowTreeNode) node;
				int num = treenode.numberOfChildren();
				for( int i = 0; i < num; i++ )
				{
					clearStatuses(treenode.getChild(i));
				}
			}
			// clear the next sibling if in a serial node
			WorkflowNode parent = node.getParent();
			if( parent.canHaveSiblingRejectPoints() )
			{
				int index = parent.indexOfChild(node) + 1;
				if( index < parent.numberOfChildren() )
				{
					clearStatuses(parent.getChild(index));
				}
			}
		}
	}

	private NodeStatus addWrappedStatus(Map<String, NodeStatus> statusMap, WorkflowNodeStatus stat)

	{
		NodeStatus nodestat = null;
		char type = stat.getNode().getType();
		switch( type )
		{
			case WorkflowNode.ITEM_TYPE:
				nodestat = new TaskStatus(stat, this);
				break;
			case WorkflowNode.SERIAL_TYPE:
				nodestat = new SerialStatus(stat, this);
				break;
			case WorkflowNode.PARALLEL_TYPE:
				nodestat = new ParallelStatus(stat, this);
				break;
			case WorkflowNode.DECISION_TYPE:
				nodestat = new DecisionStatus(stat, this, itemService);
				break;
			case WorkflowNode.SCRIPT_TYPE:
				nodestat = new ScriptStatus(stat, this, itemService);
				break;

			default:
				// Shouldn't be any others
				break;
		}
		statusMap.put(stat.getNode().getUuid(), nodestat);
		return nodestat;
	}

	public Map<String, NodeStatus> initStatusMap(Collection<WorkflowNodeStatus> statuses)
	{
		params.clearAllStatuses();
		Map<String, NodeStatus> statusMap = params.getStatusMap();
		if( statuses != null )
		{
			for( WorkflowNodeStatus stat : statuses )
			{
				if( stat.getStatus() != WorkflowNodeStatus.ARCHIVED )
				{
					addWrappedStatus(statusMap, stat);
				}
			}

			Workflow workflow;
			try
			{
				workflow = getWorkflow();
			}
			catch( Exception e )
			{
				throw new WorkflowException(e);
			}
			WorkflowNode root = workflow.getRoot();
			List<NodeStatus> created = new ArrayList<NodeStatus>();
			recurseStatuses(root, created);
			for( NodeStatus statuscreated : created )
			{
				statuscreated.update();
			}
			Iterator<NodeStatus> statVals = statusMap.values().iterator();
			while( statVals.hasNext() )
			{
				NodeStatus statbean = statVals.next();
				if( statbean.getWorkflowNode() == null )
				{
					statVals.remove();
				}
			}
		}
		return statusMap;
	}

	public Map<String, NodeStatus> getStatusMap()
	{
		Map<String, NodeStatus> statusMap = params.getStatusMap();
		if( statusMap == null )
		{
			ModerationStatus status = getModerationStatus();
			if( status != null )
			{
				return initStatusMap(status.getStatuses());
			}
		}
		return statusMap;
	}

	public NodeStatus[] getChildStatuses(WorkflowNode node)
	{
		if( node.isLeafNode() )
		{
			return new NodeStatus[0];
		}
		Map<String, NodeStatus> statmap = getStatusMap();
		WorkflowTreeNode treenode = (WorkflowTreeNode) node;
		int num = treenode.numberOfChildren();
		NodeStatus[] ret = new NodeStatus[num];
		for( int i = 0; i < num; i++ )
		{
			WorkflowNode child = treenode.getChild(i);
			NodeStatus stat = statmap.get(child.getUuid());
			ret[i] = stat;
		}
		return ret;
	}

	public NodeStatus getNodeStatus(String uuid)
	{
		Map<String, NodeStatus> map = getStatusMap();
		if( map == null )
		{
			return null;
		}
		else
		{
			return map.get(uuid);
		}
	}

	public void updateModeration()
	{
		ModerationStatus status = getModerationStatus();
		Set<WorkflowNodeStatus> statuses = new HashSet<WorkflowNodeStatus>();
		boolean clearAll = true;
		if( getItem().isModerating() )
		{
			clearAll = false;
			Workflow workflow = getWorkflow();
			WorkflowNode root = workflow.getRoot();
			NodeStatus nodeStatus = getNodeStatus(root.getUuid());
			fillStatuses(statuses, root);
			if( nodeStatus != null && nodeStatus.getStatus() == WorkflowNodeStatus.COMPLETE )
			{
				clearAll = true;
				makeLive(true);
			}
		}
		Set<WorkflowNodeStatus> oldStatuses = status.getStatuses();
		if( clearAll )
		{
			workflowService.cleanupMessageFiles(oldStatuses);
			oldStatuses.clear();
		}
		else
		{
			for( WorkflowNodeStatus oldStatus : oldStatuses )
			{
				if( !statuses.contains(oldStatus) )
				{
					oldStatus.archive();
				}
			}
			oldStatuses.addAll(statuses);
		}
	}

	private void fillStatuses(Set<WorkflowNodeStatus> statuses, WorkflowNode node)

	{
		NodeStatus nodeStatus = getNodeStatus(node.getUuid());
		if( nodeStatus != null )
		{
			WorkflowNodeStatus bean = nodeStatus.getBean();
			statuses.add(bean);
			if( !node.isLeafNode() )
			{
				WorkflowTreeNode treenode = (WorkflowTreeNode) node;
				int num = treenode.numberOfChildren();
				for( int i = 0; i < num; i++ )
				{
					WorkflowNode child = treenode.getChild(i);
					fillStatuses(statuses, child);
				}
			}
		}
	}

	private boolean recurseStatuses(WorkflowNode node, List<NodeStatus> created)

	{
		boolean hasKids = false;
		String id = node.getUuid();
		NodeStatus stat = getNodeStatus(id);
		if( !node.isLeafNode() )
		{
			WorkflowTreeNode treenode = (WorkflowTreeNode) node;
			int num = treenode.numberOfChildren();
			for( int i = 0; i < num; i++ )
			{
				hasKids |= recurseStatuses(treenode.getChild(i), created);
			}
		}
		if( stat == null && hasKids )
		{
			WorkflowNodeStatus statbean = new WorkflowNodeStatus(node);
			statbean.setStatus(WorkflowNodeStatus.INCOMPLETE);
			stat = addWrappedStatus(getStatusMap(), statbean);
			created.add(stat);
		}
		if( stat != null )
		{
			stat.setWorkflowNode(node);
		}
		return stat != null;
	}

	public void removeModerationNotifications()
	{
		removeNotificationsForItem(getItemId(), Notification.REASON_MODERATE, Notification.REASON_OVERDUE,
			Notification.REASON_REJECTED);
	}
	
	public void addModerationNotifications(ItemKey itemKey, Collection<String> users, String reason, boolean batched)
	{
		String collectionUuid = getItem().getItemDefinition().getUuid();
		
		Multimap<String, String> collectionOptOut = notificationPrefs.getOptedOutCollectionsForUsers(users);
		HashSet<String> userSet = new HashSet<>(users);
		userSet.removeAll(collectionOptOut.get(collectionUuid));
		if (!userSet.isEmpty())
		{
			addNotifications(itemKey, userSet, reason, batched);
		}
	}


	public boolean canCurrentUserModerate(WorkflowItem item, WorkflowItemStatus status)
	{
		return workflowService.canCurrentUserModerate(getItemXml(), item, status);
	}

	public Set<String> getUsersToModerate(WorkflowItem item)
	{
		return workflowService.getAllModeratorUserIDs(getItemXml(), item);
	}

	public Set<String> getUsersToNotifyOnScriptError(ScriptNode node)
	{
		return workflowService.getUsersToNotifyOnScriptError(node);
	}

	public Set<String> getUsersToNotifyOnScriptCompletion(ScriptNode node)
	{
		return workflowService.getUsersToNotifyOnScriptCompletion(node);
	}

	public boolean overrideScript(DecisionStatus status)
	{
		return false;
	}

	protected void addMessage(String taskId, char type, String msg, String uuid)
	{
		WorkflowMessage message = new WorkflowMessage();
		message.setDate(params.getDateNow());
		message.setUser(CurrentUser.getUserID());
		if( uuid == null )
		{
			uuid = UUID.randomUUID().toString();
		}
		message.setUuid(uuid);
		message.setMessage(msg);
		message.setType(type);
		NodeStatus nodeStatus = getNodeStatus(taskId);
		nodeStatus.getBean().getComments().add(message);
	}

	public void enterTask(final WorkflowItem task)
	{
		if( params.isUpdate() )
		{
			taskStatsService.enterTask(getItem(), task, params.getDateNow());
		}
		else
		{
			params.addAfterSaveHook(new Runnable()
			{

				@Override
				public void run()
				{
					taskStatsService.enterTask(getItem(), task, params.getDateNow());
				}
			});
		}
	}

	public void exitTask(final WorkflowItem task)
	{
		if( params.isUpdate() )
		{
			taskStatsService.exitTask(getItem(), task, params.getDateNow());
		}
		else
		{
			params.addAfterSaveHook(new Runnable()
			{
				@Override
				public void run()
				{
					taskStatsService.exitTask(getItem(), task, params.getDateNow());
				}
			});
		}
	}

	public void exitTasksForItem()
	{
		if( params.isUpdate() )
		{
			taskStatsService.exitAllTasksForItem(getItem(), params.getDateNow());
		}
	}

	public void restoreTasksForItem()
	{
		Item item = getItem();
		if( item.isModerating() )
		{
			taskStatsService.restoreTasksForItem(item);
		}
	}

	@Override
	public ItemOperationParams getParams()
	{
		return super.getParams();
	}

	@Override
	public PropBagEx getItemXml()
	{
		return super.getItemXml();
	}
}
