package com.tle.web.workflow.notification

import javax.inject.Singleton

import com.tle.beans.item.Item
import com.tle.common.usermanagement.user.valuebean.UserBean
import com.tle.core.guice.Bind
import com.tle.core.notification.beans.Notification
import com.tle.web.sections.render.{AppendedLabel, Label}
import com.tle.web.sections.result.util.KeyLabel
import com.tle.web.workflow.notification.NotificationLangStrings.KEYPFX_EMAIL_SUBJECT

case class ScriptTaskGroup(reason: String, taskName: String) extends NotificationGroup
{
  override def templateName: String = "notification-script.ftl"

  def headerLabel(user: UserBean, total: Int): Label = AppendedLabel.get(
    NotificationLangStrings.userHeaderLabel(user),
    new KeyLabel(NotificationLangStrings.pluralKey(NotificationLangStrings.KEY_HEADER+reason, total), taskName)
  )

  def subjectLabel: Label = new KeyLabel(KEYPFX_EMAIL_SUBJECT+reason, taskName)
}

@Bind
@Singleton
class ScriptTaskNotification extends FilterableNotification with TemplatedNotification with NotificationLookup {
  type N = ScriptNotification

  case class ScriptNotification(note: Notification, item: Item) extends TaskNotification
  {
    def group = ScriptTaskGroup(note.getReason, getTaskName.getText)
  }

  def toFreemarkerModel(notes: Iterable[Notification]) = createDataIgnore(notes, ScriptNotification)

}


