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


trait NotificationGroup
{
  def headerLabel(user: UserBean, total: Int): Label
  def subjectLabel: Label
  def templateName: String
}

case class StdNotificationGroup(templateName: String, reason: String) extends NotificationGroup
{
  def headerLabel(user: UserBean, total: Int): Label = NotificationLangStrings.header(user, reason, total)

  def subjectLabel: Label = NotificationLangStrings.subject(reason)
}

trait NotificationModel
{
  def group: NotificationGroup
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

  def emails(user: UserBean, notifications: Iterable[Notification], totals: Map[String, Int]): Iterable[NotificationEmail] = {
    toFreemarkerModel(notifications).groupBy(_.group).map {
      case (group, notes) =>
        val writer = new StringWriter()
        viewFactory.render(viewFactory.createResultWithModel(group.templateName,
          new StandardEmailModel(group.headerLabel(user, notes.size), notes)), writer)
        NotificationEmail(group.subjectLabel.getText, writer.toString, notes.map(_.note))
    }
  }

}
