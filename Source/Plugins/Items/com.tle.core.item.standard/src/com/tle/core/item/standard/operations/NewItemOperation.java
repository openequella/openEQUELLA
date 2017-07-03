package com.tle.core.item.standard.operations;

import javax.inject.Inject;

import com.google.common.collect.Multimap;
import com.google.inject.assistedinject.Assisted;
import com.tle.core.notification.beans.Notification;

public class NewItemOperation extends AbstractStandardWorkflowOperation
{
	private Multimap<String, String> collectionMap;

	@Inject
	public NewItemOperation(@Assisted Multimap<String, String> collectionMap)
	{
		this.collectionMap = collectionMap;
	}

	@Override
	public boolean execute()
	{
		addNotifications(getItemId(), collectionMap.get(getCollection().getUuid()), Notification.REASON_WENTLIVE2,
			true);
		return false;
	}
}
