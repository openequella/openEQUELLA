/*
 * Created on Aug 25, 2005
 */
package com.tle.core.workflow.operations.tasks;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import com.dytech.edge.exceptions.WorkflowException;
import com.tle.beans.item.HistoryEvent;
import com.tle.beans.item.HistoryEvent.Type;
import com.tle.beans.item.ModerationStatus;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.workflow.Workflow;
import com.tle.common.workflow.WorkflowItemStatus;
import com.tle.common.workflow.WorkflowMessage;
import com.tle.common.workflow.WorkflowNodeStatus;
import com.tle.common.workflow.node.WorkflowItem;
import com.tle.common.workflow.node.WorkflowNode;
import com.tle.common.workflow.node.WorkflowTreeNode;
import com.tle.core.notification.beans.Notification;
import com.tle.core.services.entity.WorkflowService;
import com.tle.core.user.CurrentUser;
import com.tle.core.workflow.nodes.DecisionStatus;
import com.tle.core.workflow.nodes.NodeStatus;
import com.tle.core.workflow.nodes.ParallelStatus;
import com.tle.core.workflow.nodes.SerialStatus;
import com.tle.core.workflow.nodes.TaskStatus;
import com.tle.core.workflow.operations.AbstractWorkflowOperation;

public abstract class TaskOperation extends AbstractWorkflowOperation
{
	@Inject
	private WorkflowService workflowService;

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

	public void resetWorkflow()
	{
		Workflow workflow = getWorkflow();

		removeModerationNotifications();

		ModerationStatus status = getModerationStatus();
		status.setNeedsReset(false);
		status.getStatuses().clear();
		params.clearAllStatuses();

		exitTasksForItem();
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
				nodestat = new DecisionStatus(stat, this, workflowOpService);
				break;

			default:
				// Shouldn't be any others
				break;
		}
		statusMap.put(stat.getNode().getUuid(), nodestat);
		return nodestat;
	}

	public Map<String, NodeStatus> getStatusMap()
	{
		Map<String, NodeStatus> statusMap = params.getStatusMap();
		if( statusMap == null )
		{
			ModerationStatus status = getModerationStatus();
			if( status != null )
			{
				params.clearAllStatuses();
				statusMap = params.getStatusMap();
				Set<WorkflowNodeStatus> statuses = status.getStatuses();
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

	public boolean canCurrentUserModerate(WorkflowItem item, WorkflowItemStatus status)
	{
		return workflowService.canCurrentUserModerate(getItemXml(), item, status);
	}

	public Set<String> getUsersToModerate(WorkflowItem item)
	{
		return workflowService.getAllModeratorUserIDs(getItemXml(), item);
	}

	public boolean overrideScript(DecisionStatus status)
	{
		return false;
	}

	protected void addMessage(String taskId, char type, String msg)
	{
		WorkflowMessage message = new WorkflowMessage();
		message.setDate(params.getDateNow());
		message.setUser(CurrentUser.getUserID());
		message.setMessage(msg);
		message.setType(type);
		NodeStatus nodeStatus = getNodeStatus(taskId);
		nodeStatus.getBean().getComments().add(message);
	}

}
