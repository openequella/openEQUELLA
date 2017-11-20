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

package com.tle.core.workflow.event;

import java.util.Set;

import com.tle.common.workflow.node.WorkflowNode;
import com.tle.core.events.ApplicationEvent;
import com.tle.core.workflow.event.listener.WorkflowChangeListener;

public class WorkflowChangeEvent extends ApplicationEvent<WorkflowChangeListener>
{
	private static final long serialVersionUID = 1L;

	private final boolean delete;
	private final Set<WorkflowNode> nodes;
	private final long workflowId;

	public WorkflowChangeEvent(long workflowId, Set<WorkflowNode> nodes, boolean delete)
	{
		super(PostTo.POST_TO_SELF_SYNCHRONOUSLY);
		this.workflowId = workflowId;
		this.nodes = nodes;
		this.delete = delete;
	}

	@Override
	public void postEvent(WorkflowChangeListener listener)
	{
		listener.workflowChange(this);
	}

	@Override
	public Class<WorkflowChangeListener> getListener()
	{
		return WorkflowChangeListener.class;
	}

	public boolean isDelete()
	{
		return delete;
	}

	public Set<WorkflowNode> getNodes()
	{
		return nodes;
	}

	public long getWorkflowId()
	{
		return workflowId;
	}
}
