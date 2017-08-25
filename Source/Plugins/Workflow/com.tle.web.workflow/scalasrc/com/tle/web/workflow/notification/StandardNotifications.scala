package com.tle.web.workflow.notification

import java.io.StringWriter
import javax.inject.{Inject, Singleton}

import com.tle.common.usermanagement.user.valuebean.UserBean
import com.tle.core.guice.Bind
import com.tle.core.i18n.BundleCache
import com.tle.core.item.service.ItemService
import com.tle.core.notification.{NotificationEmail, NotificationExtension}
import com.tle.core.notification.beans.Notification
import com.tle.web.sections.equella.freemarker.ExtendedFreemarkerFactory
import com.tle.web.sections.render.Label
import com.tle.web.viewurl.ViewItemUrlFactory
import scala.collection.JavaConverters._

@Bind
@Singleton
class StandardNotifications extends FilterableNotification with TemplatedNotification with NotificationLookup {

  type N = ItemNotification
  override def templateName: String = "notification-item.ftl"

  def toFreemarkerModel(notes: Iterable[Notification]) = createItemNotifications(notes)
}


