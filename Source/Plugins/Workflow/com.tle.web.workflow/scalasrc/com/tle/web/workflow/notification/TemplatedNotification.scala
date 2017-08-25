package com.tle.web.workflow.notification

import java.io.StringWriter
import javax.inject.Inject

import com.tle.common.usermanagement.user.valuebean.UserBean
import com.tle.core.notification.NotificationEmail
import com.tle.core.notification.beans.Notification
import com.tle.web.sections.equella.freemarker.ExtendedFreemarkerFactory
import com.tle.web.sections.render.Label
import NotificationLangStrings._
import scala.collection.JavaConverters._


trait NotificationModel
{
  def group: String = note.getReason
  def note: Notification
}

trait TemplatedNotification {
  @Inject
  var viewFactory: ExtendedFreemarkerFactory = _
  type N <: NotificationModel

  def toFreemarkerModel(notes: Iterable[Notification]): Iterable[N]

  class StandardEmailModel(val getHeader: Label, notifications: Iterable[N])
  {
    def getNotifications = notifications.asJava
  }

  def templateName: String

  def emails(user: UserBean, notifications: Iterable[Notification], totals: Map[String, Int]): Iterable[NotificationEmail] = {
    toFreemarkerModel(notifications).groupBy(_.group).map {
      case (reason, notes) =>
        val writer = new StringWriter()
        viewFactory.render(viewFactory.createResultWithModel(templateName,
          new StandardEmailModel(header(user, reason, notes.size), notes)), writer)
        NotificationEmail(subject(reason), writer.toString, notes.map(_.note))
    }
  }

}
