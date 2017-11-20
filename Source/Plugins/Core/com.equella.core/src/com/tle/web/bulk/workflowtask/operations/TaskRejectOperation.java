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

package com.tle.web.bulk.workflowtask.operations;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.beans.item.*;
import com.tle.beans.item.HistoryEvent.Type;
import com.tle.common.workflow.WorkflowItemStatus;
import com.tle.common.workflow.WorkflowMessage;
import com.tle.common.workflow.node.WorkflowNode;
import com.tle.core.item.standard.workflow.nodes.TaskStatus;
import com.tle.core.notification.beans.Notification;

public class TaskRejectOperation extends AbstractBulkTaskOperation
{
	private final String message;

	@AssistedInject
	public TaskRejectOperation(@Assisted("message") String message)
	{
		this.message = message;
	}

	@Override
	public boolean execute()
	{
		TaskStatus status = init("REJECT_BULK_TASKS", "MANAGE_WORKFLOW");
		ItemTaskId taskId = getTaskId();

		ModerationStatus modstatus = getModerationStatus();
		modstatus.setLastAction(params.getDateNow());
		HistoryEvent reject = createHistory(Type.rejected);
		reject.setComment(message);
		setStepFromTask(reject, taskId.getTaskId());
		addMessage(taskId.getTaskId(), WorkflowMessage.TYPE_REJECT, message, null);

		WorkflowItemStatus bean = (WorkflowItemStatus) status.getBean();
		params.setCause(bean);

		// EQ-2546 Once rejected, the item moves to the closest previous
		// rejection point.
		WorkflowNode rejectNode = status.getClosestRejectNode();
		if( rejectNode != null )
		{
			setToStepFromTask(reject, rejectNode.getUuid());
			reenter(rejectNode);
		}
		else
		{
			setToStepFromTask(reject, null);
			Item item = getItem();
			setState(ItemStatus.REJECTED);
			modstatus.setRejectedMessage(message);
			modstatus.setRejectedBy(getUserId());
			modstatus.setRejectedStep(status.getId());
			exitTasksForItem();
			item.setModerating(false);
			removeModerationNotifications();
			addNotifications(item.getItemId(), getAllOwnerIds(), Notification.REASON_REJECTED, false);
		}
		updateModeration();
		return true;
	}
}
