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

  def toFreemarkerModel(notes: Iterable[Notification]) = createItemNotifications("notification-item.ftl", notes)
}


