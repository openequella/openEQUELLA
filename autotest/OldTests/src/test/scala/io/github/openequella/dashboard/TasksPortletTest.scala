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

package io.github.openequella.dashboard

import com.tle.webtests.framework.TestInstitution
import io.github.openequella.pages.dashboard.PortletType
import io.github.openequella.pages.dashboard.portlets.PortletFactory.Tasks
import io.github.openequella.pages.dashboard.portlets.TasksPortlet
import org.testng.Assert.assertTrue
import org.testng.annotations.{BeforeClass, BeforeMethod, Test}
import testng.annotation.NewUIOnly

@NewUIOnly
@TestInstitution("rest")
class TasksPortletTest extends AbstractPortletTest {
  override protected def loginWithPortletAccount(): Unit = logon()

  var taskPortletName: String    = _
  var tasksPortlet: TasksPortlet = _

  @BeforeClass
  def setupClass(): Unit = {
    taskPortletName = context.getFullName("task portlet")

    loginWithPortletAccount()
    loadDashboardPage()
    // Create Tasks portlet
    dashboardPage.createPortlet(PortletType.Tasks, taskPortletName)
  }

  @BeforeMethod
  def setupTasksPortletTest(): Unit = {
    tasksPortlet = dashboardPage.getPortlet(Tasks, taskPortletName)
  }

  @Test(description = "Should be able to see counts for all tasks and it's sub task")
  def allTasks(): Unit = {
    assertTrue(tasksPortlet.hasCountForAllTasks)
    assertTrue(tasksPortlet.hasCountForTaskAssignedToMe)
  }

  @Test(description =
    "Should be able to see counts for all notifications and it's sub notification"
  )
  def allNotifications(): Unit = {
    assertTrue(tasksPortlet.hasCountForAllNotifications)
    assertTrue(tasksPortlet.hasCountForOverdueModeration)
  }
}
