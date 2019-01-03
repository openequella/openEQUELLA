/*
 * Licensed to the Apereo Foundation under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.tle.web.workflow.notification

import com.tle.web.notification.WebNotificationExtension
import com.tle.web.sections.result.util.KeyLabel
import com.tle.web.workflow.notification.FilterableNotification._

object FilterableNotification
{
  import NotificationLangStrings.r

  val KEY_REASON_FILTER = r.key("notereason.")
  val KEY_REASON_LIST = r.key("notificationlist.reasons.")
}

trait FilterableNotification extends WebNotificationExtension {
  def getReasonLabel(str: String) = new KeyLabel(KEY_REASON_LIST + str)
  def getReasonFilterLabel(str: String) = new KeyLabel(KEY_REASON_FILTER + str)
  def isIndexed(ntype: String) = true
}
