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


