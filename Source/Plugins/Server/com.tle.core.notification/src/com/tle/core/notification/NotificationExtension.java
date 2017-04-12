package com.tle.core.notification;

import com.google.common.collect.ListMultimap;
import com.tle.core.notification.beans.Notification;

public interface NotificationExtension
{
	boolean isIndexed(String type);

	boolean isForceEmail(String type);

	String emailText(ListMultimap<String, Notification> typeMap);

}
