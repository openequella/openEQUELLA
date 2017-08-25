package com.tle.core.notification

import com.tle.core.notification.beans.Notification

case class NotificationEmail(subject: String, text: String, pertainsTo: Iterable[Notification])
