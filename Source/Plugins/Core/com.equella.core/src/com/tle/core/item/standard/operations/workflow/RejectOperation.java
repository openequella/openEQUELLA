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

import java.io.IOException;

import com.dytech.edge.exceptions.WorkflowException;
import com.google.common.base.Throwables;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.annotation.Nullable;
import com.tle.beans.item.HistoryEvent;
import com.tle.beans.item.HistoryEvent.Type;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemStatus;
import com.tle.beans.item.ModerationStatus;
import com.tle.common.Check;
import com.tle.common.filesystem.handle.StagingFile;
import com.tle.common.workflow.WorkflowMessage;
import com.tle.common.workflow.node.WorkflowNode;
import com.tle.core.filesystem.WorkflowMessageFile;
import com.tle.core.item.standard.workflow.nodes.TaskStatus;
import com.tle.core.notification.beans.Notification;
import com.tle.core.security.impl.SecureInModeration;

/**
 * @author jmaginnis
 */
// Sonar maintains that 'Class cannot be instantiated and does not provide any
// static methods or fields', but methinks thats bunkum
@SecureInModeration
public final class RejectOperation extends SpecificTaskOperation // NOSONAR
{
	private final String msg;
	private final String tostep;
	private final String messageUuid;

	@AssistedInject
	private RejectOperation(@Assisted("taskId") String taskId, @Assisted("comment") String msg,
		@Assisted("step") @Nullable String tostep, @Assisted("messageUuid") @Nullable String messageUuid)
	{
		super(taskId);
		this.msg = msg;
		this.tostep = tostep;
		this.messageUuid = messageUuid;
	}

	@SuppressWarnings("nls")
	@Override
	public boolean execute()
	{
		checkWeCanModerate();
		ModerationStatus modstatus = getModerationStatus();
		modstatus.setLastAction(params.getDateNow());

		TaskStatus status = getTaskStatus();
		params.setCause(status.getBean());
		HistoryEvent reject = createHistory(Type.rejected);
		reject.setComment(msg);
		setToStepFromTask(reject, tostep);
		setStepFromTask(reject);
		addMessage(WorkflowMessage.TYPE_REJECT, msg, messageUuid);

		if( Check.isEmpty(tostep) )
		{
			Item item = getItem();
			setState(ItemStatus.REJECTED);
			modstatus.setRejectedMessage(msg);
			modstatus.setRejectedBy(getUserId());
			modstatus.setRejectedStep(status.getId());
			exitTasksForItem();
			item.setModerating(false);
			removeModerationNotifications();
			addNotifications(item.getItemId(), getAllOwnerIds(), Notification.REASON_REJECTED, false);
		}
		else
		{
			WorkflowNode parentNode = status.getRejectNode(tostep);
			if( parentNode != null )
			{
				reenter(parentNode);
			}
			else
			{
				throw new WorkflowException("Rejection step is not a parent");
			}
		}
		updateModeration();

		if (messageUuid != null)
		{
			try
			{
				fileSystemService.commitFiles(new StagingFile(messageUuid), new WorkflowMessageFile(messageUuid));
			}
			catch (IOException ex)
			{
				throw Throwables.propagate(ex);
			}
		}
		return true;
	}
}
