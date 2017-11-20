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

import com.dytech.edge.exceptions.WorkflowException;
import com.tle.beans.item.HistoryEvent;
import com.tle.core.item.standard.workflow.nodes.TaskStatus;

public abstract class SpecificTaskOperation extends TaskOperation
{
	private String taskId;

	public SpecificTaskOperation(String taskId)
	{
		this.taskId = taskId;
	}

	public TaskStatus getTaskStatus()
	{
		return (TaskStatus) getNodeStatus(taskId);
	}

	@SuppressWarnings("nls")
	public void checkWeCanModerate()
	{
		if( !getTaskStatus().canCurrentUserModerate(this) )
		{
			throw new WorkflowException("Current user can't moderate this step");
		}
	}

	protected void addMessage(char type, String message)
	{
		addMessage(type, message, null);
	}

	protected void addMessage(char type, String message, String uuid)
	{
		addMessage(taskId, type, message, uuid);
	}

	protected void setStepFromTask(HistoryEvent event)
	{
		setStepFromTask(event, taskId);
	}

}
