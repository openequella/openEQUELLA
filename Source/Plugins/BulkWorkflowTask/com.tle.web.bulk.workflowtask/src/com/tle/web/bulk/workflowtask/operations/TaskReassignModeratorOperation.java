package com.tle.web.bulk.workflowtask.operations;

import java.util.Set;

import javax.inject.Inject;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.ModerationStatus;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.item.standard.workflow.nodes.TaskStatus;
import com.tle.core.notification.beans.Notification;
import com.tle.core.notification.standard.service.NotificationPreferencesService;
import com.tle.core.security.impl.SecureInModeration;
import com.tle.exceptions.AccessDeniedException;

@SecureInModeration
public final class TaskReassignModeratorOperation extends AbstractBulkTaskOperation
{
	private final String toUser;

	@Inject
	private NotificationPreferencesService notificationPreferencesService;

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
			status.setAssignedTo(toUser);

			ItemId itemId = getItemId();
			ItemKey itemKey = new ItemId(itemId.getUuid(), itemId.getVersion());
			notificationService.addNotification(itemKey, Notification.REASON_REASSIGN, toUser, false);
			ModerationStatus moderation = getItem().getModeration();
			if( moderation != null && moderation.getRejectedBy() == null )
			{
				moderation.setRejectedBy(toUser);
			}

			String collectionUuid = getItem().getItemDefinition().getUuid();
			if( !notificationPreferencesService.getOptedOutCollectionsForUser(toUser).contains(collectionUuid) )
			{
				notificationService.addNotification(itemId, Notification.REASON_REASSIGN, toUser, false);
				getParams().setNotificationsAdded(true);
			}

			return true;
		}

		throw new AccessDeniedException(CurrentLocale.get("com.tle.core.services.item.error.notmoderatingtask",
			CurrentLocale.get(getItem().getName()), CurrentLocale.get(status.getWorkflowNode().getName())));
	}
}
