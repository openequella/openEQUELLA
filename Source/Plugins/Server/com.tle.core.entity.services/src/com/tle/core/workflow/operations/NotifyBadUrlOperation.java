package com.tle.core.workflow.operations;

import com.tle.core.notification.beans.Notification;

public class NotifyBadUrlOperation extends AbstractWorkflowOperation
{
	@Override
	public boolean execute()
	{
		addNotifications(getItemId(), getAllOwnerIds(), Notification.REASON_BADURL, true);
		return true;
	}
}
