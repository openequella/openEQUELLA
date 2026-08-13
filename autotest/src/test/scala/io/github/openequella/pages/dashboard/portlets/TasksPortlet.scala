/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.openequella.pages.dashboard.portlets

import com.tle.webtests.framework.PageContext
import org.openqa.selenium.By

import scala.jdk.CollectionConverters._

/** Represents a Task Portlet.
  *
  * @param context
  *   The PageContext for the current test session.
  * @param name
  *   The name of the Task Portlet.
  */
class TasksPortlet(context: PageContext, name: String)
    extends GenericPortlet[TasksPortlet](context, name) {
  private val ALL_TASKS_TEXT           = "All tasks"
  private val TASK_ASSIGNED_TO_ME_TEXT = "Tasks assigned to me"
  private val ALL_NOTIFICATIONS_TEXT   = "All notifications"
  private val OVERDUE_MODERATION_TEXT  = "Resources that are overdue to be moderated"

  validationXpath = s"$portletXpath//span[text()='$ALL_TASKS_TEXT']"

  /** Check if the portlet has a count for "All Tasks".
    */
  def hasCountForAllTasks: Boolean = hasCountFor(ALL_TASKS_TEXT)

  /** Check if the portlet has a count for "Tasks assigned to me".
    */
  def hasCountForTaskAssignedToMe: Boolean = hasCountFor(TASK_ASSIGNED_TO_ME_TEXT)

  /** Check if the portlet has a count for "All notifications".
    */
  def hasCountForAllNotifications: Boolean = hasCountFor(ALL_NOTIFICATIONS_TEXT)

  /** Check if the portlet has a count for "Resources that are overdue to be moderated".
    */
  def hasCountForOverdueModeration: Boolean = hasCountFor(
    OVERDUE_MODERATION_TEXT
  )

  private def countXpath(record: String) =
    s"$portletXpath//span[text()='$record']//following-sibling::div/span[contains(@class,'MuiChip-label')]"

  private def hasCountFor(record: String): Boolean = {
    val xpath = countXpath(record)
    driver.findElements(By.xpath(xpath)).asScala.nonEmpty
  }
}
