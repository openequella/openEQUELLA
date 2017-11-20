/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.web.workflow.notification

import javax.inject.Singleton

import com.tle.beans.item.Item
import com.tle.common.usermanagement.user.valuebean.UserBean
import com.tle.core.guice.Bind
import com.tle.core.notification.NotificationEmail
import com.tle.core.notification.beans.Notification
import com.tle.web.notification.WebNotificationExtension
import scala.collection.JavaConverters._

@Bind
@Singleton
class BadURLNotifications extends FilterableNotification with TemplatedNotification with NotificationLookup {
  type N = BadURLModel
  case class BadURLModel(note: Notification, item: Item) extends ItemNotification
  {
    lazy val getUrls = item.getReferencedUrls.asScala.filterNot(_.isSuccess).asJava
    def group = StdNotificationGroup("notification-badurl.ftl", note.getReason)
  }

  def toFreemarkerModel(notes: Iterable[Notification]) = createDataIgnore(notes, BadURLModel)
}
