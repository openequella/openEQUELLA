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
		TaskStatus status = init();
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
