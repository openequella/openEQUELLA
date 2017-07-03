package com.tle.core.notification;

import java.util.Collection;

import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemKey;
import com.tle.core.notification.beans.Notification;
import com.tle.core.services.impl.ClusteredTask;

public interface NotificationService
{
	void addNotification(ItemKey itemId, String reason, String userTo, boolean batched);

	void addNotification(String uuid, String itemOnlyId, String reason, String userUuid, boolean batched);

	void addNotifications(ItemKey itemId, String reason, Collection<String> userUuids, boolean batched);

	void addNotifications(String uuid, String reason, Collection<String> users, boolean batched);

	void removeNotification(long notificationId);

	Notification getNotification(long notificationId);

	void processEmails();

	NotificationExtension getExtensionForType(String type);

	Collection<String> getNotificationTypes();

	int removeAllForItem(ItemId itemId, String reason);

	int removeAllForUuid(String uuid, String reason);

	int removeAllForUuid(String uuid, Collection<String> reasons);

	int removeAllForItem(ItemId itemId, Collection<String> reasons);

	int removeAllForKey(ItemKey itemKey, String reason);

	int removeAllForKey(ItemKey itemKey, Collection<String> reasons);

	int removeForUserAndKey(ItemKey itemKey, String userId, String reason);

	int removeForUserAndKey(ItemKey itemKey, String userId, Collection<String> reasons);

	ClusteredTask getClusteredTask(boolean batched);

	/**
	 * @return true if there were changes.
	 */
	boolean userIdChanged(ItemKey itemKey, String fromUserId, String toUserId);
}
