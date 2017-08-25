package com.tle.web.workflow.notification

import javax.inject.Singleton

import com.tle.beans.item.{Item, ItemTaskId}
import com.tle.core.guice.Bind
import com.tle.core.notification.beans.Notification

@Bind
@Singleton
class ReassignNotification extends FilterableNotification with TemplatedNotification with NotificationLookup {
  type N = ReassignNoteModel
  override def templateName: String = "notification-reassigned.ftl"

  case class ReassignNoteModel(note: Notification, item: Item) extends TaskNotification

  def toFreemarkerModel(notes: Iterable[Notification]) = createDataIgnore(notes, ReassignNoteModel)
}


