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

}
