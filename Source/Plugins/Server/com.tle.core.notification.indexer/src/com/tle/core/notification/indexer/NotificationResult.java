package com.tle.core.notification.indexer;

import com.tle.beans.item.ItemIdKey;
import com.tle.core.services.item.FreetextResult;

public class NotificationResult extends FreetextResult
{
	private static final long serialVersionUID = 1L;

	private long notificationId;

	public NotificationResult(ItemIdKey key, long notificationId, float relevance, boolean sortByRelevance)
	{
		super(key, relevance, sortByRelevance);
		this.notificationId = notificationId;
	}

	public long getNotificationId()
	{
		return notificationId;
	}

}
