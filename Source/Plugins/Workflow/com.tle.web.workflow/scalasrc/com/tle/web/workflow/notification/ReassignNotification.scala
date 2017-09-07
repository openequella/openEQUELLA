package com.tle.web.workflow.notification

import javax.inject.{Inject, Singleton}

import com.tle.beans.item.{Item, ItemTaskId}
import com.tle.core.guice.Bind
import com.tle.core.notification.beans.Notification
import com.tle.core.services.user.{LazyUserLookup, UserService}

@Bind
@Singleton
class ReassignNotification extends FilterableNotification with TemplatedNotification with NotificationLookup {
  type N = ReassignNoteModel
  @Inject
  var userService: UserService = _

  case class ReassignNoteModel(lul: LazyUserLookup)(val note: Notification, val item: Item) extends TaskNotification with OwnerLookup
  {
    def group = StdNotificationGroup("notification-reassigned.ftl", note.getReason)
  }

  def toFreemarkerModel(notes: Iterable[Notification]) = createDataIgnore(notes, ReassignNoteModel(new LazyUserLookup(userService)))
}


