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
import com.tle.webtests.pageobject.portal.RecentContributionsEditPage
import io.github.openequella.pages.dashboard.DashboardPage
import io.github.openequella.pages.search.ItemStatus.ItemStatus
import org.openqa.selenium.By

/** Represents a Recent Portlet.
  *
  * @param context
  *   The PageContext for the current test session.
  * @param name
  *   The name of the Recent Portlet.
  */
class RecentPortlet(context: PageContext, name: String)
    extends GenericPortlet[RecentPortlet](context, name) {
  private val NO_RESULTS_TEXT = "No recent contributions found."

  // No contributions message or a list of contributions
  validationXpath = s"$portletXpath//span[text()='$NO_RESULTS_TEXT'] | $portletXpath//ul//a"

  /** Checks if the provided item title exists in the Recent Portlet contributions.
    */
  def hasContribution(title: String): Boolean = isVisible(
    By.xpath(s"$portletXpath//span[text()='$title']")
  )

  /** Checks if the provided description exists in the Recent Portlet contributions.
    */
  def hasDescription(description: String): Boolean = isVisible(
    By.xpath(s"$portletXpath//p[text()='$description']")
  )

  /** Edits the Recent Portlet with the provided parameters.
    *
    * @param status
    *   The status to filter contributions (e.g., "live", "draft").
    * @param query
    *   The query string to filter contributions.
    * @param displayTitleOnly
    *   Whether to display only titles.
    */
  def edit(
      status: Option[ItemStatus] = None,
      query: Option[String] = None,
      displayTitleOnly: Option[Boolean] = None
  ): DashboardPage = {
    edit()

    val editPage = new RecentContributionsEditPage(context).get()

    status.map(_.toString.toLowerCase).foreach(editPage.setStatus)
    query.foreach(editPage.setQuery)
    displayTitleOnly.foreach(editPage.setDisplayTitleOnly)

    editPage.save(new DashboardPage(context))
  }
}
