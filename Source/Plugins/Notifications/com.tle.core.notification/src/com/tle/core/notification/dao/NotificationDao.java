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

package com.tle.core.notification.dao;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.tle.beans.Institution;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemKey;
import com.tle.core.hibernate.dao.GenericInstitutionalDao;
import com.tle.core.notification.beans.Notification;

public interface NotificationDao extends GenericInstitutionalDao<Notification, Long>
{
	Notification getExistingNotification(ItemKey itemId, String reason, String user);

	Notification getExistingNotification(String uuid, String reason, String user);

	List<Notification> getNotificationsForItem(ItemId itemId, Institution institution);

	void deleteAllForInstitution(Institution institution);

	void deleteAllForItem(ItemId itemId);

	int deleteAllForItemReasons(ItemId itemId, Collection<String> reasons);

	int deleteAllForKeyReasons(ItemKey itemKey, Collection<String> reasons);

	int deleteAllForUserKeyReasons(ItemKey itemKey, String userId, Collection<String> reasons);

	int deleteAllForUuidReasons(String uuid, Collection<String> reasons);

	boolean userIdChanged(ItemKey itemKey, String fromUserId, String toUserId);

	NotifiedUser getUserToNotify(Date notAfter, String attemptId, boolean batched);

	int updateLastAttempt(String user, boolean batched, Date date, String attemptId);

	List<Notification> getNewestNotificationsForUser(int maximum, String user, Collection<String> reasons,
		String attemptId);

	Map<String, Integer> getReasonCounts(String user, String attemptId);

	int markProcessed(String user, Collection<String> reasons, String attemptId);

	int deleteUnindexed(String user, Collection<String> reasons, String attemptId);

	int markProcessedById(String user, Collection<Long> notifications, String attemptId);

	int deleteUnindexedById(String user, Collection<Long> notifications, String attemptId);

}
