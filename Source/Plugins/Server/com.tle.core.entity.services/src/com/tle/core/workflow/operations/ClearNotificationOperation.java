package com.tle.core.workflow.operations;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

public class ClearNotificationOperation extends AbstractWorkflowOperation
{
	private final long notificationId;

	@AssistedInject
	public ClearNotificationOperation(@Assisted long notificationId)
	{
		this.notificationId = notificationId;
	}

	@Override
	public boolean execute()
	{
		notificationService.removeNotification(notificationId);
		return true;
	}

}
