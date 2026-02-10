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
import com.tle.webtests.pageobject.portal.TaskStatisticsPortalEditPage
import com.tle.webtests.pageobject.tasklist.ManageTasksPage
import io.github.openequella.Locators.{byAriaLabel, byTestId}
import io.github.openequella.pages.dashboard.TaskTrend.TaskTrend
import io.github.openequella.pages.dashboard.{DashboardPage, TaskTrend}
import io.github.openequella.pages.manage.ManageResourcesPage
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.{By, WebElement}

/** Represents a Task statistics Portlet.
  *
  * @param context
  *   The PageContext for the current test session.
  * @param name
  *   The name of the Task statistics Portlet.
  */
class TaskStatisticsPortlet(context: PageContext, name: String)
    extends GenericPortlet[TaskStatisticsPortlet](context, name) {
  val NO_RESULTS_TEXT       = "There are no results for the selected workflow."
  val PERMISSION_ERROR_TEXT = "You do not have permission to manage any workflows."

  private val noPermissionXpath = s"$portletXpath//div[text()='$PERMISSION_ERROR_TEXT']"
  private val noResultsXpath    = s"$portletXpath//span[text()='$NO_RESULTS_TEXT']"

  validationXpath = s"$noPermissionXpath | $noResultsXpath | $portletXpath//table"

  /** Selects a workflow from the workflow selector.
    *
    * @param workflow
    *   The name of the workflow to select.
    */
  def selectWorkflow(workflow: String): Unit = {
    selectOption(By.xpath(s"$portletXpath//div[@aria-labelledby='workflow-label']"), workflow)
    waitForTrendData
  }

  /** Selects a trend from the trend toggle buttons.
    *
    * @param trend
    *   The trend to select.
    */
  def selectTrend(trend: TaskTrend): Unit = {
    getTrendButton(trend).click()
    waitForTrendData
  }

  /** Clicks on a task link in the portlet, which should navigate to the manage tasks page with the
    * appropriate filters applied.
    *
    * @param taskName
    *   The name of the task to click.
    */
  def clickTask(taskName: String): ManageTasksPage = {
    val task = By.xpath(s"$portletXpath//button[text()='$taskName']")
    driver.findElement(task).click()
    new ManageTasksPage(context).get()
  }

  /** Clicks on the item count link in the portlet, which should navigate to the manage resources
    * page with the appropriate filters applied.
    */
  def clickItemCount(): ManageResourcesPage = {
    val link = byTestId("task-statistics-item-count-link")
    driver.findElement(link).click()

    new ManageResourcesPage(context).get()
  }

  /** Edits the Task statistics Portlet with the provided trend.
    *
    * @param trend
    *   The trend to set in the edit page.
    */
  def edit(trend: TaskTrend): DashboardPage = {
    val editPage = new TaskStatisticsPortalEditPage(context).get()
    editPage.setTrend(trend.toString)
    editPage.save(this)

    new DashboardPage(context).get()
  }

  /** Checks if the given trend is currently selected in the portlet.
    *
    * @param trend
    *   The trend to check.
    */
  def isTrendSelected(trend: TaskTrend): Boolean =
    getTrendButton(trend).getAttribute("aria-pressed") == "true"

  /** Checks if the portlet is showing the permission denied message.
    */
  def isPermissionDenied: Boolean = isVisible(By.xpath(noPermissionXpath))

  /** Checks if the portlet is showing the no results message.
    */
  def isNoResults: Boolean = isVisible(By.xpath(noResultsXpath))

  private def getTrendButton(trend: TaskTrend): WebElement = {
    val trendText = trend match {
      case TaskTrend.WEEK  => "Weekly"
      case TaskTrend.MONTH => "Monthly"
    }

    val button = byAriaLabel(trendText)
    driver.findElement(button)
  }

  // Wait for the trend to load by checking for the presence of the table or the no results message.
  private def waitForTrendData =
    waiter.until(ExpectedConditions.presenceOfElementLocated(By.xpath(validationXpath)))
}
