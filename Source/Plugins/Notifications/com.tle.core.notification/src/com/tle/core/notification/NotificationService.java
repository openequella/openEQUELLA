/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
