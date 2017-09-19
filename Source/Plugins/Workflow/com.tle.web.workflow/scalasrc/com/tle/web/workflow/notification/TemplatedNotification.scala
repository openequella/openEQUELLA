package com.tle.web.workflow.notification

import java.io.StringWriter
import javax.inject.Inject

import com.tle.common.usermanagement.user.valuebean.UserBean
import com.tle.core.notification.NotificationEmail
import com.tle.core.notification.beans.Notification
import com.tle.web.sections.equella.freemarker.ExtendedFreemarkerFactory
import com.tle.web.sections.render.{AppendedLabel, Label}
import NotificationLangStrings._

import scala.collection.JavaConverters._


trait NotificationGroup
{
  def headerHello(user: UserBean): Label
  def headerReason(total: Int): Label
  def subjectLabel: Label
  def templateName: String
}

case class StdNotificationGroup(templateName: String, reason: String) extends NotificationGroup
{
  def headerHello(user: UserBean): Label = NotificationLangStrings.userHeaderLabel(user)
  def headerReason(total: Int): Label = NotificationLangStrings.headerLabel(reason, total)
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

  class StandardEmailModel(val getHello: Label, val getReason: Label, notifications: Iterable[N])
  {
    def getNotifications = notifications.asJava
    def getHeader = AppendedLabel.get(getHello, getReason)
  }

  def emails(user: UserBean, notifications: Iterable[Notification], totals: Map[String, Int]): Iterable[NotificationEmail] = {
    toFreemarkerModel(notifications).groupBy(_.group).map {
      case (group, notes) =>
        val writer = new StringWriter()
        viewFactory.render(viewFactory.createResultWithModel(group.templateName,
          new StandardEmailModel(group.headerHello(user), group.headerReason(notes.size), notes)), writer)
        NotificationEmail(group.subjectLabel.getText, writer.toString, notes.map(_.note))
    }
  }

}
