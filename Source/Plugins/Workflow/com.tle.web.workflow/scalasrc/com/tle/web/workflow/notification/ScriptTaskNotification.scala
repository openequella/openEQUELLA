package com.tle.web.workflow.notification

import java.io.StringWriter
import javax.inject.{Inject, Singleton}

import com.tle.common.usermanagement.user.valuebean.UserBean
import com.tle.core.guice.Bind
import com.tle.core.notification.NotificationEmail
import com.tle.core.notification.beans.Notification
import com.tle.web.sections.equella.freemarker.ExtendedFreemarkerFactory
import com.tle.web.sections.render.Label

import scala.collection.JavaConverters._

@Bind
@Singleton
class ScriptTaskNotification extends FilterableNotification with TemplatedNotification with NotificationLookup {
  type N = ItemNotification
  def toFreemarkerModel(notes: Iterable[Notification]) = createItemNotifications(notes)

  def templateName = "notification-script.ftl"
}


