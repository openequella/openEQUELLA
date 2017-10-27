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

package com.tle.core.item.standard.workflow.nodes;

import java.text.ParseException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.Sets;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemTaskId;
import com.tle.common.Check;
import com.tle.common.i18n.CurrentTimeZone;
import com.tle.common.util.Dates;
import com.tle.common.util.LocalDate;
import com.tle.common.workflow.WorkflowItemStatus;
import com.tle.common.workflow.WorkflowNodeStatus;
import com.tle.common.workflow.node.ScriptNode;
import com.tle.common.workflow.node.WorkflowItem;
import com.tle.common.workflow.node.WorkflowItem.MoveLive;
import com.tle.common.workflow.node.WorkflowItem.Priority;
import com.tle.common.workflow.node.WorkflowNode;
import com.tle.common.workflow.node.WorkflowTreeNode;
import com.tle.core.item.NodeStatus;
import com.tle.core.item.operations.ItemOperationParams;
import com.tle.core.item.standard.operations.workflow.TaskOperation;
import com.tle.core.notification.beans.Notification;

public class TaskStatus extends AbstractNodeStatus
{
	private final WorkflowItemStatus taskbean;

	public TaskStatus(WorkflowNodeStatus bean, TaskOperation op)
	{
		super(bean, op);
		if( !(bean instanceof WorkflowItemStatus) )
		{
			throw new ClassCastException("bean required to be WorkflowItemStatus, but is " //$NON-NLS-1$
				+ bean.getClass().getSimpleName());
		}
		this.taskbean = (WorkflowItemStatus) bean;
	}

	public boolean canCurrentUserModerate(TaskOperation op)
	{
		return op.canCurrentUserModerate((WorkflowItem) node, taskbean);
	}

	public Set<String> getUsersToModerate(TaskOperation op)
	{
		return op.getUsersToModerate((WorkflowItem) node);
	}

	@Override
	public boolean update()
	{
		WorkflowItem item = (WorkflowItem) node;
		Set<String> acceptedUsers = taskbean.getAcceptedUsers();
		Set<String> usersToModerate = op.getUsersToModerate(item);
		boolean unan = item.isUnanimousacceptance();
		if( unan )
		{
			if( acceptedUsers.containsAll(usersToModerate) )
			{
				return finished();
			}
		}
		else if( !acceptedUsers.isEmpty() || (usersToModerate.isEmpty() && Check.isEmpty(item.getRoles())) )
		{
			return finished();
		}

		return false;
	}

	@Override
	public boolean finished()
	{
		clear();
		final WorkflowItem task = (WorkflowItem) node;
		if( task.getMovelive() == MoveLive.ACCEPTED )
		{
			op.makeLive(false);
		}
		return super.finished();
	}

	@Override
	public void clear()
	{
		op.removeNotificationsForKey(getTaskKey(), Notification.REASON_MODERATE, Notification.REASON_OVERDUE);
		if( taskbean.getStatus() == WorkflowNodeStatus.INCOMPLETE )
		{
			op.exitTask((WorkflowItem) node);
		}
	}

	public void addAccepted(String userId)
	{
		op.removeNotificationForUserAndKey(getTaskKey(), userId, Notification.REASON_MODERATE,
			Notification.REASON_OVERDUE);
		taskbean.addAccepted(userId);
	}

	public void setAssignedTo(String userId)
	{
		taskbean.setAssignedTo(userId);
	}

	public String getAssignedTo()
	{
		return taskbean.getAssignedTo();
	}

	@Override
	public void enter()
	{
		bean.setStatus(WorkflowNodeStatus.INCOMPLETE);
		taskbean.setStarted(op.getParams().getDateNow());
		final WorkflowItem task = (WorkflowItem) node;
		Set<String> usersToModerate = op.getUsersToModerate(task);
		op.addModerationNotifications(getTaskKey(), usersToModerate, Notification.REASON_MODERATE,
			task.getPriority() <= Priority.LOW.intValue());

		op.enterTask(task);

		processAutoAssign(task, usersToModerate);
		if( task.getMovelive() == MoveLive.ARRIVAL )
		{
			op.makeLive(false);
		}
		if( task.isEscalate() )
		{
			setupDueDate(task);
		}
		update();
	}

	private void processAutoAssign(WorkflowItem task, Set<String> usersToModerate)
	{
		final Item aitem = op.getItem();
		Set<String> autoAssignsByStep = task.getAutoAssigns();
		final String autoAssignByXPath = task.getAutoAssignNode();
		if( Check.isEmpty(autoAssignsByStep) )
		{
			autoAssignsByStep = findAllPreviousSteps();
		}

		// EQ-2388 shizzle
		if( usersToModerate.size() == 1 )
		{
			setAssignedTo(usersToModerate.iterator().next());
		}

		boolean nonefound = true;
		for( String stepUuid : autoAssignsByStep )
		{
			NodeStatus nodeStatus = op.getNodeStatus(stepUuid);
			if( nodeStatus != null )
			{
				String assignedTo = ((TaskStatus) nodeStatus).getAssignedTo();
				if( !Check.isEmpty(assignedTo) && usersToModerate.contains(assignedTo) )
				{
					setAssignedTo(assignedTo);
					nonefound = false;
					break;
				}
			}
		}
		if( nonefound && autoAssignsByStep.contains("") //$NON-NLS-1$
			&& usersToModerate.contains(aitem.getOwner()) )
		{
			setAssignedTo(aitem.getOwner());
		}
		else if( !Check.isEmpty(autoAssignByXPath) )
		{
			setAssignedTo(op.getItemXml().getNode(autoAssignByXPath));
		}

	}

	private void setupDueDate(WorkflowItem task)
	{
		ItemOperationParams params = op.getParams();
		Date dateDue = null;
		if( !Check.isEmpty(task.getDueDatePath()) )
		{
			String dateStr = op.getItemXml().getNode(task.getDueDatePath());
			if( !Check.isEmpty(dateStr) )
			{
				try
				{
					dateDue = new LocalDate(dateStr, Dates.ISO_DATE_ONLY, CurrentTimeZone.get()).toDate();
				}
				catch( ParseException e )
				{
					// ignore bad dates
				}
			}
		}
		if( dateDue == null )
		{
			dateDue = new Date(params.getDateNow().getTime() + TimeUnit.DAYS.toMillis(task.getEscalationdays()));
		}
		taskbean.setDateDue(dateDue);
	}

	private Set<String> findAllPreviousSteps()
	{
		Set<String> previousSteps = Sets.newHashSet();
		WorkflowNode currnode = node;
		WorkflowNode parent = node.getParent();
		while( parent != null )
		{
			if( parent.canHaveSiblingRejectPoints() )
			{
				int i = parent.indexOfChild(currnode) - 1;
				while( i >= 0 )
				{
					WorkflowNode child = parent.getChild(i);
					if( child.getType() == WorkflowNode.ITEM_TYPE )
					{
						previousSteps.add(child.getUuid());
					}
					i--;
				}
			}

			currnode = parent;
			parent = parent.getParent();
		}
		return previousSteps;
	}

	private ItemTaskId getTaskKey()
	{
		return new ItemTaskId(op.getItem().getItemId(), node.getUuid());
	}

	public WorkflowNode getRejectNode(String taskid)
	{
		WorkflowNode currnode = node;
		WorkflowNode parent = node.getParent();
		while( parent != null )
		{
			if( parent.getUuid().equals(taskid) )
			{
				return parent.isRejectPoint() ? parent : null;
			}

			if( parent.canHaveSiblingRejectPoints() )
			{
				int i = parent.indexOfChild(currnode) - 1;
				while( i >= 0 )
				{
					WorkflowNode child = parent.getChild(i);
					if( child.getUuid().equals(taskid) && isNodeRejectPoint(child) )
					{
						return child;
					}
					i--;
				}
			}

			currnode = parent;
			parent = parent.getParent();
		}
		return null;
	}

	public WorkflowNode getClosestRejectNode()
	{
		WorkflowNode currnode = node;
		WorkflowNode parent = node.getParent();
		while( parent != null )
		{
			if( parent.isRejectPoint() && !(parent instanceof ScriptNode) )
			{
				return parent;
			}

			if( parent.canHaveSiblingRejectPoints() )
			{
				int i = parent.indexOfChild(currnode) - 1;
				while( i >= 0 )
				{
					WorkflowNode child = parent.getChild(i);
					if( isNodeRejectPoint(child) && !(child instanceof ScriptNode) )
					{
						return child;
					}
					i--;
				}
			}
			currnode = parent;
			parent = parent.getParent();
		}
		return null;
	}

	private boolean isNodeRejectPoint(WorkflowNode node)
	{
		if( node instanceof WorkflowItem )
		{
			return ((WorkflowItem) node).isRejectPoint();
		}
		else if( node instanceof WorkflowTreeNode )
		{
			return ((WorkflowTreeNode) node).isRejectPoint();
		}
		return false;
	}

	public Collection<String> getUsersLeft(TaskOperation op)
	{
		WorkflowItem item = (WorkflowItem) node;
		Set<String> acceptedUsers = taskbean.getAcceptedUsers();
		Set<String> usersToModerate = op.getUsersToModerate(item);
		if( usersToModerate == null )
		{
			return Collections.emptyList();
		}
		usersToModerate.removeAll(acceptedUsers);
		return usersToModerate;
	}

}
