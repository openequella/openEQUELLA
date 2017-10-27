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

import com.tle.common.workflow.WorkflowNodeStatus;
import com.tle.common.workflow.node.ParallelNode;
import com.tle.common.workflow.node.WorkflowNode;
import com.tle.core.item.NodeStatus;
import com.tle.core.item.standard.operations.workflow.TaskOperation;

public class ParallelStatus extends AbstractNodeStatus
{
	public ParallelStatus(WorkflowNodeStatus bean, TaskOperation op)
	{
		super(bean, op);
	}

	@Override
	public boolean update()
	{
		boolean updated = false;
		NodeStatus[] childStatuses = op.getChildStatuses(node);
		ParallelNode parnode = (ParallelNode) node;
		int num = parnode.numberOfChildren();
		for( int i = 0; i < num; i++ )
		{
			WorkflowNode child = parnode.getChild(i);
			if( childStatuses[i] == null )
			{
				updated = true;
				op.enter(child);
				childStatuses = op.getChildStatuses(node);
			}
		}
		if (bean.getStatus() == WorkflowNodeStatus.INCOMPLETE)
		{
			if (allComplete(childStatuses))
			{
				updated |= finished();
			}
		}
		return updated;
	}

	@Override
	public void enter()
	{
		bean.setStatus(WorkflowNodeStatus.INCOMPLETE);
		update();
	}
}
