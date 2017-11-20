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

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.beans.item.HistoryEvent;
import com.tle.beans.item.HistoryEvent.Type;
import com.tle.core.item.standard.workflow.nodes.TaskStatus;
import com.tle.core.security.impl.SecureInModeration;

@SecureInModeration
public class AssignOperation extends SpecificTaskOperation
{
	@AssistedInject
	public AssignOperation(@Assisted String taskId)
	{
		super(taskId);
	}

	@Override
	public boolean execute()
	{
		TaskStatus status = getTaskStatus();
		String assignedTo = status.getAssignedTo();
		HistoryEvent event = createHistory(Type.assign);
		if( assignedTo != null && assignedTo.equals(getUserId()) )
		{
			status.setAssignedTo(null);
			event.setUser(null);
		}
		else
		{
			status.setAssignedTo(getUserId());
		}
		setStepFromTask(event);
		updateModeration();
		return true;
	}
}
