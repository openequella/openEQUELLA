package com.tle.web.notification;

import com.tle.core.notification.NotificationExtension;
import com.tle.web.sections.render.Label;

public interface WebNotificationExtension extends NotificationExtension
{
	Label getReasonLabel(String type);

	Label getReasonFilterLabel(String type);
}
