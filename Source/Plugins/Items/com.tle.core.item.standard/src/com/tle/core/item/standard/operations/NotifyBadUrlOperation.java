package com.tle.core.item.standard.operations;

import com.tle.core.notification.beans.Notification;

public class NotifyBadUrlOperation extends AbstractStandardWorkflowOperation
{
	@Override
	public boolean execute()
	{
		addNotifications(getItemId(), getAllOwnerIds(), Notification.REASON_BADURL, true);
		return true;
	}
}
