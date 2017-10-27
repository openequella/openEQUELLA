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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.dytech.edge.exceptions.WorkflowException;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemStatus;
import com.tle.beans.workflow.WorkflowStatus;
import com.tle.beans.workflow.WorkflowStep;
import com.tle.common.workflow.Workflow;
import com.tle.common.workflow.WorkflowItemStatus;
import com.tle.common.workflow.WorkflowNodeStatus;
import com.tle.common.workflow.node.WorkflowItem;
import com.tle.common.workflow.node.WorkflowNode;
import com.tle.common.workflow.node.WorkflowTreeNode;
import com.tle.core.guice.Bind;
import com.tle.core.item.NodeStatus;
import com.tle.core.workflow.events.WorkflowEvent;

/**
 * @author jmaginnis
 */
@Bind
public class StatusOperation extends TaskOperation
{
	private static final String MODERATE_ITEM = "MODERATE_ITEM"; //$NON-NLS-1$

	protected WorkflowStatus status;

	protected StatusOperation()
	{
		super();
	}

	@Override
	public boolean isReadOnly()
	{
		return true;
	}

	@Override
	public boolean execute()
	{
		try
		{
			WorkflowStatus state = new WorkflowStatus();
			Item item = getItem();
			if( item != null )
			{
				state.setModerationAllowed(params.getSecurityStatus().getAllowedPrivileges().contains(MODERATE_ITEM));
				state.setOwnerId(item.getOwner());
				state.setStatusName(getItemStatus());
				state.setOwner(itemService.isAnOwner(item, getUserId()));
				state.setModerating(item.isModerating());
				state.setRejected(checkStatus(ItemStatus.REJECTED));
				setupEvents(state);
			}
			else
			{
				state.setStatusName(ItemStatus.DRAFT);
			}
			state.setSecurityStatus(params.getSecurityStatus());
			this.status = state;
		}
		catch( Exception e )
		{
			throw new WorkflowException(e);
		}
		return false;
	}

	private void setupEvents(WorkflowStatus status)
	{
		try
		{
			Workflow workflow = getWorkflow();
			Map<String, WorkflowStep> map = new HashMap<String, WorkflowStep>();

			if( workflow != null )
			{
				recurseWorkflow(status, workflow.getRoot(), map);
			}

			WorkflowEvent[] allEvents = WorkflowEvent.getAllEvents(getItem());
			status.setEvents(allEvents);
			status.setReferencedSteps(map);
		}
		catch( Exception e )
		{
			throw new WorkflowException(e);
		}
	}

	private void recurseWorkflow(WorkflowStatus state, WorkflowNode node, Map<String, WorkflowStep> map)
	{
		if( !node.isLeafNode() )
		{
			WorkflowTreeNode treenode = (WorkflowTreeNode) node;
			int num = treenode.numberOfChildren();
			for( int i = 0; i < num; i++ )
			{
				WorkflowNode child = treenode.getChild(i);
				recurseWorkflow(state, child, map);
			}
		}

		WorkflowStep step;
		if( node instanceof WorkflowItem )
		{
			WorkflowItem item = (WorkflowItem) node;
			WorkflowItemStatus bean = null;
			NodeStatus nodeStatus = getNodeStatus(node.getUuid());
			if( nodeStatus != null && nodeStatus.getStatus() == WorkflowNodeStatus.INCOMPLETE )
			{
				bean = (WorkflowItemStatus) nodeStatus.getBean();
			}

			step = new WorkflowStep(item, bean);
			if( bean != null )
			{
				Set<String> users = workflowService.getAllModeratorUserIDs(getItemXml(), item);
				users.removeAll(bean.getAcceptedUsers());
				step.setToModerate(users);
				state.addCurrentStep(step);
			}

			List<WorkflowStep> parents = new ArrayList<WorkflowStep>();
			WorkflowNode parent = item.getParent();
			addRejectPoints(parents, parent, node);
			step.setRejectPoints(parents);
		}
		else
		{
			step = new WorkflowStep(node.getUuid(), node.getName());
		}
		map.put(node.getUuid(), step);
	}

	private void addRejectPoints(List<WorkflowStep> parents, WorkflowNode parent, WorkflowNode node)
	{
		if( parent.canHaveSiblingRejectPoints() )
		{
			int i = parent.indexOfChild(node) - 1;
			while( i >= 0 )
			{
				WorkflowNode child = parent.getChild(i);
				if( isNodeRejectPoint(child) )
				{
					parents.add(new WorkflowStep(child.getUuid(), child.getName()));
				}
				i--;
			}
		}

		if( parent.isRejectPoint() )
		{
			parents.add(new WorkflowStep(parent.getUuid(), parent.getName()));
		}

		WorkflowNode newparent = parent.getParent();
		if( newparent != null )
		{
			addRejectPoints(parents, newparent, parent);
		}
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

	public WorkflowStatus getStatus()
	{
		return status;
	}
}
