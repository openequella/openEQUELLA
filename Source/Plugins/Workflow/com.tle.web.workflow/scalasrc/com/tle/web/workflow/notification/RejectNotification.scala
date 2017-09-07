package com.tle.web.workflow.notification

import javax.inject.{Inject, Singleton}

import com.tle.beans.item.Item
import com.tle.core.guice.Bind
import com.tle.core.notification.beans.Notification
import com.tle.core.services.user.{LazyUserLookup, UserService}
import com.tle.web.sections.render.TextLabel
import com.tle.web.sections.result.util.{BundleLabel, UserLabel}

import scala.collection.JavaConverters._
@Bind
@Singleton
class RejectNotification extends FilterableNotification with TemplatedNotification with NotificationLookup {

  @Inject
  var userService: UserService = _

  type N = RejectedNote
  case class RejectedNote(lul: LazyUserLookup)(val note: Notification, val item: Item) extends ItemNotification
  {
    def group = StdNotificationGroup("notification-rejected.ftl", note.getReason)
    val modStatus = item.getModeration

    val getRejectedMessage = new TextLabel(modStatus.getRejectedMessage)
    val getRejectedBy = new UserLabel(modStatus.getRejectedBy, lul)
    val getRejectedTask =
      Option(item.getItemDefinition.getWorkflow)
        .flatMap(_.getNodes.asScala.find(_.getUuid == modStatus.getRejectedStep))
        .map(n => new BundleLabel(n.getName, bundleCache)).getOrElse(NotificationLangStrings.unknownTask(modStatus.getRejectedStep))
  }

  def toFreemarkerModel(notes: Iterable[Notification]) = createDataIgnore(notes, RejectedNote(new LazyUserLookup(userService)))
}
