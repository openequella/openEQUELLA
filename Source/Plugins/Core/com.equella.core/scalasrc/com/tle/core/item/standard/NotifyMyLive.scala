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

package com.tle.core.item.standard

import com.tle.core.item.standard.operations.AbstractStandardWorkflowOperation
import com.tle.core.notification.beans.Notification
import com.tle.core.services.user.UserPreferenceService

import scala.collection.JavaConverters._

object NotifyMyLive {

  def notifyOwners(op: AbstractStandardWorkflowOperation, ups: UserPreferenceService): Unit = {
    val item = op.getItem
    val users = ups.getPreferenceForUsers(UserPreferenceService.NOTIFY_MYLIVE,
      (item.getCollaborators.asScala.toBuffer :+ item.getOwner).asJavaCollection).asScala.collect {
      case (u, "true") => u
    }
    op.addNotifications(item.getItemId, users.asJavaCollection, Notification.REASON_MYLIVE, false)
  }

}
