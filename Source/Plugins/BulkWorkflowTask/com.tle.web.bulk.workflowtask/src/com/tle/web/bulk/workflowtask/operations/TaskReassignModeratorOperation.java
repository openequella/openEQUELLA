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

import java.util.Collections;
import java.util.Set;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.item.standard.workflow.nodes.TaskStatus;
import com.tle.core.notification.beans.Notification;
import com.tle.core.security.impl.SecureInModeration;
import com.tle.exceptions.AccessDeniedException;

@SecureInModeration
public final class TaskReassignModeratorOperation extends AbstractBulkTaskOperation
{
	private final String toUser;

	@AssistedInject
	private TaskReassignModeratorOperation(@Assisted("toUser") String toUser)
	{
		this.toUser = toUser;
	}

	@Override
	public boolean execute()
	{
		TaskStatus status = init("MANAGE_WORKFLOW");
		Set<String> usersToModerate = status.getUsersToModerate(this);
		if( usersToModerate.contains(toUser) )
		{
			String oldUser = status.getAssignedTo();
			if (toUser.equals(oldUser))
			{
				return false;
			}
			status.setAssignedTo(toUser);
			if (oldUser != null)
			{
				removeNotificationForUserAndKey(getTaskId(), oldUser, Notification.REASON_REASSIGN);
			}
			addModerationNotifications(getTaskId(), Collections.singleton(toUser), Notification.REASON_REASSIGN, false);
			return true;
		}

		throw new AccessDeniedException(CurrentLocale.get("com.tle.core.services.item.error.notmoderatingtask",
			CurrentLocale.get(getItem().getName()), CurrentLocale.get(status.getWorkflowNode().getName())));
	}
}
