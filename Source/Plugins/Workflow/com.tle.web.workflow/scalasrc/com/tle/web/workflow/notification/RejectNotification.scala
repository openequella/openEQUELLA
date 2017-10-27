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
