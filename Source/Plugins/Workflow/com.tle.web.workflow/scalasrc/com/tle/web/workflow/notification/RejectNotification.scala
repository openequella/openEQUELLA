package com.tle.web.workflow.notification

import javax.inject.{Inject, Singleton}

import com.tle.beans.item.Item
import com.tle.common.usermanagement.user.valuebean.UserBean
import com.tle.core.guice.Bind
import com.tle.core.notification.NotificationEmail
import com.tle.core.notification.beans.Notification
import com.tle.core.services.user.{LazyUserLookup, UserService}
import com.tle.web.notification.WebNotificationExtension
import com.tle.web.sections.Bookmark
import com.tle.web.sections.render.{Label, TextLabel}
import com.tle.web.sections.result.util.UserLabel

@Bind
@Singleton
class RejectNotification extends FilterableNotification with TemplatedNotification with NotificationLookup {

  @Inject
  var userService: UserService = _

  type N = RejectedNote
  case class RejectedNote(lul: LazyUserLookup)(val note: Notification, val item: Item) extends ItemNotification
  {
    val modStatus = item.getModeration
    val getRejectMessage = NotificationLangStrings.somethingBy(new TextLabel(modStatus.getRejectedMessage),
      new UserLabel(modStatus.getRejectedBy, lul))
  }

  def toFreemarkerModel(notes: Iterable[Notification]) = createDataIgnore(notes, RejectedNote(new LazyUserLookup(userService)))

  def templateName = "notification-rejected.ftl"
}
