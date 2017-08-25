package com.tle.web.workflow.notification

import javax.inject.Singleton

import com.tle.beans.item.Item
import com.tle.common.usermanagement.user.valuebean.UserBean
import com.tle.core.guice.Bind
import com.tle.core.notification.NotificationEmail
import com.tle.core.notification.beans.Notification
import com.tle.web.notification.WebNotificationExtension
import scala.collection.JavaConverters._

@Bind
@Singleton
class BadURLNotifications extends FilterableNotification with TemplatedNotification with NotificationLookup {
  type N = BadURLModel
  case class BadURLModel(note: Notification, item: Item) extends ItemNotification
  {
    lazy val getUrls = item.getReferencedUrls.asScala.filterNot(_.isSuccess).asJava
  }

  def toFreemarkerModel(notes: Iterable[Notification]) = createDataIgnore(notes, BadURLModel)

  def templateName = "notification-badurl.ftl"
}
