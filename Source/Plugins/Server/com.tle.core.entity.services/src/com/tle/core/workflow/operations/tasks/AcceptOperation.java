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

package com.tle.core.workflow.operations.tasks;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.annotation.Nullable;
import com.tle.beans.item.HistoryEvent;
import com.tle.beans.item.HistoryEvent.Type;
import com.tle.common.Check;
import com.tle.common.workflow.WorkflowMessage;
import com.tle.core.security.impl.SecureInModeration;
import com.tle.core.workflow.nodes.TaskStatus;

/**
 * @author jmaginnis
 */
// Sonar maintains that 'Class cannot be instantiated and does not provide any
// static methods or fields', but methinks thats bunkum
@SecureInModeration
public final class AcceptOperation extends SpecificTaskOperation // NOSONAR
{
	private final String message;

	@AssistedInject
	private AcceptOperation(@Assisted("taskId") String taskId, @Assisted("comment") @Nullable String message)
	{
		super(taskId);
		this.message = message;
	}

	@Override
	public boolean execute()
	{
		checkWeCanModerate();
		TaskStatus status = getTaskStatus();
		params.setCause(status.getBean());
		getModerationStatus().setLastAction(params.getDateNow());
		status.addAccepted(getUserId());
		HistoryEvent approved = createHistory(Type.approved);
		setStepFromTask(approved);
		if( !Check.isEmpty(message) )
		{
			approved.setComment(message);
			addMessage(WorkflowMessage.TYPE_ACCEPT, message);
		}
		status.update();
		updateModeration();
		return true;
	}

}
