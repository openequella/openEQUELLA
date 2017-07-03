package com.tle.core.item.standard.operations;

import java.util.Set;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.beans.item.Item;

// Sonar maintains that 'Class cannot be instantiated and does not provide any
// static methods or fields', but methinks thats bunkum
public class ModifyNotificationsOperation extends AbstractStandardWorkflowOperation // NOSONAR
{
	private Set<String> userIds;

	@AssistedInject
	private ModifyNotificationsOperation(@Assisted Set<String> userIds)
	{
		this.userIds = userIds;
	}

	@Override
	public boolean execute()
	{
		Item item = getItem();
		item.setNotifications(userIds);
		return true;
	}
}
