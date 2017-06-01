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

package com.tle.core.workflow.nodes;

import com.tle.common.workflow.WorkflowNodeStatus;
import com.tle.common.workflow.node.WorkflowNode;
import com.tle.core.workflow.operations.tasks.TaskOperation;

public abstract class NodeStatus
{
	protected WorkflowNodeStatus bean;
	protected WorkflowNode node;
	protected TaskOperation op;

	public NodeStatus(WorkflowNodeStatus bean, TaskOperation op)
	{
		this.bean = bean;
		this.op = op;
	}

	public String getId()
	{
		return bean.getNode().getUuid();
	}

	public void setWorkflowNode(WorkflowNode node)
	{
		this.node = node;
	}

	public boolean finished()
	{
		bean.setStatus(WorkflowNodeStatus.COMPLETE);
		op.update(node.getParent());
		return true;
	}

	protected boolean allComplete(NodeStatus[] childStatuses)
	{
		for( NodeStatus status : childStatuses )
		{
			if( status == null || status.getStatus() == WorkflowNodeStatus.INCOMPLETE )
			{
				return false;
			}
		}
		return true;
	}

	public int getStatus()
	{
		return bean.getStatus();
	}

	public boolean isComplete()
	{
		return getStatus() == WorkflowNodeStatus.COMPLETE;
	}

	public WorkflowNodeStatus getBean()
	{
		return bean;
	}

	public void setBean(WorkflowNodeStatus bean)
	{
		this.bean = bean;
	}

	public abstract boolean update();

	public abstract void enter();

	public WorkflowNode getWorkflowNode()
	{
		return node;
	}

	public void clear()
	{
		// nothing
	}
}
