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

package com.tle.common.security;

import java.io.Serializable;

public class WorkflowTaskTarget implements Serializable
{
	private static final long serialVersionUID = 1L;

	private long workflowId;
	private final String taskId;

	public WorkflowTaskTarget(long workflowId, String taskId)
	{
		this.workflowId = workflowId;
		this.taskId = taskId;
	}

	public String getTaskId()
	{
		return taskId;
	}

	public long getWorkflowId()
	{
		return workflowId;
	}

	public void setWorkflowId(long workflowId)
	{
		this.workflowId = workflowId;
	}

	public String getTarget()
	{
		return SecurityConstants.TARGET_WORKFLOW_TASK + ':' + workflowId + ':' + taskId;
	}

	@Override
	public boolean equals(Object obj)
	{
		if( !(obj instanceof WorkflowTaskTarget) )
		{
			return false;
		}
		WorkflowTaskTarget ttarget = (WorkflowTaskTarget) obj;
		return workflowId == ttarget.workflowId && taskId.equals(ttarget.taskId);
	}

	@Override
	public int hashCode()
	{
		return (int) (workflowId ^ taskId.hashCode());
	}
}
