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
import com.tle.core.notification.beans.Notification
import com.tle.web.sections.render.{AppendedLabel, Label}
import com.tle.web.sections.result.util.KeyLabel
import com.tle.web.workflow.notification.NotificationLangStrings.KEYPFX_EMAIL_SUBJECT

case class ScriptTaskGroup(reason: String, taskName: String) extends NotificationGroup
{
  override def templateName: String = "notification-script.ftl"

  def subjectLabel: Label = new KeyLabel(KEYPFX_EMAIL_SUBJECT+reason, taskName)

  def headerHello(user: UserBean) = NotificationLangStrings.userHeaderLabel(user)

  def headerReason(total: Int) = new KeyLabel(NotificationLangStrings.pluralKey(NotificationLangStrings.KEY_HEADER+reason, total), taskName)
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


