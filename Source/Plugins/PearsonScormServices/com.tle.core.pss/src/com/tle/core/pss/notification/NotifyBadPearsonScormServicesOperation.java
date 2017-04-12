package com.tle.core.pss.notification;

import com.tle.core.workflow.operations.AbstractWorkflowOperation;

public class NotifyBadPearsonScormServicesOperation extends AbstractWorkflowOperation
{
	@Override
	public boolean execute()
	{
		addNotifications(getItemId(), getAllOwnerIds(), "badpss", true);
		return true;
	}
}
