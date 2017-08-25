package com.tle.core.notification

import com.tle.common.usermanagement.user.valuebean.UserBean
import com.tle.core.notification.beans.Notification

trait NotificationExtension {

  def isIndexed(ntype: String): Boolean

  def emails(user: UserBean, notifications: Iterable[Notification], totals: Map[String, Int]): Iterable[NotificationEmail]
}
