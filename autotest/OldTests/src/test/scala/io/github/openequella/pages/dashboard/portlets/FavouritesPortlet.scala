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
import com.tle.webtests.pageobject.searching.SearchPage
import com.tle.webtests.pageobject.viewitem.SummaryPage
import org.openqa.selenium.By

/** Represents a Favourites Portlet in the Dashboard.
  *
  * @param context
  *   The PageContext for the current test session.
  * @param name
  *   The name of the Favourites Portlet.
  */
class FavouritesPortlet(context: PageContext, name: String)
    extends GenericPortlet[FavouritesPortlet](context, name) {
  validationXpath = s"$portletXpath//button[text()='Resources']"

  /** The XPath locator for a favourite resource/search within the Favourites Portlet.
    */
  def getItemXpath(name: String): By = By.xpath(s"$portletXpath//span[text()='$name']")

  /** Checks if a resource with the given name exists in the Portlet.
    *
    * @param name
    *   The name of the resource to check for.
    */
  def hasResource(name: String): Boolean = isVisible(getItemXpath(name))

  /** Checks if a search with the given name exists in the Portlet.
    *
    * @param name
    *   The name of the resource to check for.
    */
  def hasSearch(name: String): Boolean = isVisible(getItemXpath(name))

  /** Click on a resource with the given name in the Favourites Portlet.
    *
    * @param name
    *   The name of the resource to click.
    */
  def clickResource(name: String): SummaryPage = {
    clickItem(name)
    new SummaryPage(context).get()
  }

  /** Click on a search with the given name in the Favourites Portlet.
    *
    * @param name
    *   The name of the search to click.
    */
  def clickSearch(name: String): SearchPage = {
    clickItem(name)
    new SearchPage(context).get()
  }

  private def clickItem(name: String): Unit = driver.findElement(getItemXpath(name)).click()

  /** Click the Resources tab in the Favourites Portlet.
    */
  def clickResourcesTab(): Unit = {
    val resourcesTab = By.xpath(s"$portletXpath//button[text()='Resources']")
    driver.findElement(resourcesTab).click()
  }

  /** Click the Searches tab in the Favourites Portlet.
    */
  def clickSearchesTab(): Unit = {
    val searchesTab = By.xpath(s"$portletXpath//button[text()='Searches']")
    driver.findElement(searchesTab).click()
  }

  /** Click the Show All button in the Favourites Portlet.
    */
  def clickShowAllButton(): Unit = {
    val showAllButton = By.xpath(s"$portletXpath//a[text()='Show all']")
    driver.findElement(showAllButton).click()
  }

  /** Checks if the Portlet is minimised.
    */
  override def isMinimised: Boolean = {
    val favouritesPortletContent = By.xpath("//button[text()='Resources']")
    super.isMinimised && !isPresent(favouritesPortletContent)
  }
}
