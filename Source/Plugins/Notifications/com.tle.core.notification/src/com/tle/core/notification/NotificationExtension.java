package com.tle.core.notification;

import com.google.common.collect.ListMultimap;
import com.tle.core.notification.beans.Notification;

public interface NotificationExtension
{
	boolean isIndexed(String type);

	boolean isForceEmail(String type);

	int countNotification(ListMultimap<String, Notification> typeMap);

	String emailText(ListMultimap<String, Notification> typeMap);

}
