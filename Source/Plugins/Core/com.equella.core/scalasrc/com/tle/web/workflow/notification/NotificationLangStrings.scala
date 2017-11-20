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

import com.tle.common.i18n.CurrentLocale
import NotificationLangStrings._
import com.tle.common.usermanagement.user.valuebean.UserBean
import com.tle.web.resources.ResourcesService
import com.tle.web.sections.render.{AppendedLabel, Label, TextLabel}
import com.tle.web.sections.result.util.{KeyLabel, PluralKeyLabel}
import com.tle.web.workflow.notification.TaskNotifications.getClass

object NotificationLangStrings
{
  val r = ResourcesService.getResourceHelper(getClass)

  def l(key: String) = new KeyLabel(r.key(key))

  val KEYPFX_EMAIL_SUBJECT = r.key("email.subject.")
  val KEY_HEADER = r.key("emailheader.reason.")
  val KEY_USER_HEADER = r.key("email.header")

  val KEY_MSG_FRMT = r.key("email.msgformat")
  val KEY_TASK_MISSING = r.key("task.missing")

  def subject(reason: String) : Label = new KeyLabel(KEYPFX_EMAIL_SUBJECT+reason)
  def headerLabel(reason: String, total: Int) : Label = new PluralKeyLabel(KEY_HEADER + reason, total)
  def userHeaderLabel(user: UserBean) : Label = new KeyLabel(KEY_USER_HEADER, user.getFirstName, user.getLastName, user.getUsername)
  def somethingBy(something: Label, by: Label) = new KeyLabel(KEY_MSG_FRMT, something, by)
  def unknownTask(taskId: String) = new KeyLabel(KEY_TASK_MISSING, taskId)

  def pluralKey(key: String, total: Int) : String = if (total == 1) key+".1" else key
}
