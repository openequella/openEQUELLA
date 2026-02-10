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
import com.tle.webtests.pageobject.tasklist.ManageTasksPage
import io.github.openequella.pages.dashboard.portlets.PortletFactory.TaskStatistics
import io.github.openequella.pages.dashboard.portlets.TaskStatisticsPortlet
import io.github.openequella.pages.dashboard.{PortletType, TaskTrend}
import org.testng.Assert.{assertEquals, assertTrue}
import org.testng.annotations.{BeforeClass, BeforeMethod, Test}
import testng.annotation.NewUIOnly

@NewUIOnly
@TestInstitution("rest")
class TaskStatisticsPortletTest extends AbstractPortletTest {
  override protected def loginWithPortletAccount(): Unit = logon()

  var portletName: String                          = _
  var taskStatisticsPortlet: TaskStatisticsPortlet = _
  val WORKFLOW_3_STEP                              = "3 step, priority & duedate"

  @BeforeClass
  def setupClass(): Unit = {
    portletName = context.getFullName("task statistics portlet")

    loginWithPortletAccount()
    createPortlet()
  }

  @BeforeMethod
  def setupPortletTest(): Unit = loadPortlet()

  @Test(description = "shows portlet with week trends")
  def showPortlet(): Unit = {
    assertTrue(dashboardPage.hasPortlet(portletName))
    assertTrue(taskStatisticsPortlet.isTrendSelected(TaskTrend.WEEK))
  }

  @Test(description = "updates trend table when user select different workflow")
  def selectWorkflow(): Unit = {
    taskStatisticsPortlet.selectWorkflow("Workflow with no tasks")
    assertTrue(taskStatisticsPortlet.isNoResults)
  }

  @Test(description = "redirects to manage tasks page when user clicks on task")
  def redirectToManageTasksPage(): Unit = {
    taskStatisticsPortlet.selectWorkflow(WORKFLOW_3_STEP)
    taskStatisticsPortlet.clickTask("Step 1")

    val managePage = new ManageTasksPage(context).get()
    assertTrue(managePage.isLoaded)
    assertEquals(managePage.getResultCount, 2)
  }

  @Test(description = "redirects to manage resources page when user clicks on item count")
  def redirectToManageResourcesPage(): Unit = {
    taskStatisticsPortlet.selectWorkflow(WORKFLOW_3_STEP)
    val managePage = taskStatisticsPortlet.clickItemCount()

    assertTrue(managePage.isLoaded)
    assertEquals(managePage.getResultCount, 4)
  }

  @Test(description =
    "shows permission denied message when user doesn't have MANAGE_WORKFLOW permission"
  )
  def permissionDenied(): Unit = {
    logonAsLowPrivilegeUser()
    createPortlet()
    loadPortlet()

    assertTrue(taskStatisticsPortlet.isPermissionDenied)

    loginWithPortletAccount()
  }

  private def loadPortlet(): Unit = {
    taskStatisticsPortlet = dashboardPage.getPortlet(TaskStatistics, portletName)
  }

  private def createPortlet(): Unit = {
    loadDashboardPage()
    dashboardPage.createPortlet(PortletType.TaskStatistics, portletName)
  }
}
