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
import io.github.openequella.pages.search.NewSearchPage
import org.openqa.selenium.support.ui.ExpectedCondition
import org.openqa.selenium.{By, TimeoutException, WebDriver}

import scala.jdk.CollectionConverters._

/** Represents a Quick Search Portlet.
  *
  * @param context
  *   The PageContext for the current test session.
  * @param name
  *   The name of the Quick Search Portlet.
  */
class QuickSearchPortlet(context: PageContext, name: String)
    extends GenericPortlet[QuickSearchPortlet](context, name) {
  private val SEARCH_INPUT_XPATH = s"$portletXpath//input[@type='text']"
  validationXpath = SEARCH_INPUT_XPATH

  private val NO_RESULTS_TEXT = "No results found."

  /** Enters a search query.
    */
  def search(query: String): Unit = {
    val searchBox = driver.findElement(By.xpath(SEARCH_INPUT_XPATH))
    searchBox.clear()
    searchBox.sendKeys(query)

    waitForSearchComplete()
  }

  /** Waits for the search to complete.
    */
  def waitForSearchComplete(): Unit = {
    try {
      waiter.until(new ExpectedCondition[Boolean] {
        override def apply(driver: WebDriver): Boolean = {
          val hasResultList =
            driver.findElements(By.xpath(s"$portletXpath//ul")).asScala.nonEmpty

          lazy val showsNoResultsMessage =
            driver
              .findElements(By.xpath(s"$portletXpath//div[text()='$NO_RESULTS_TEXT']"))
              .asScala
              .nonEmpty

          hasResultList || showsNoResultsMessage
        }
      })
    } catch {
      case e: TimeoutException =>
        throw new AssertionError(s"Search did not complete for portlet: $name", e)
    }
  }

  /** Checks if a search result with the given title exists.
    */
  def hasResult(title: String): Boolean = isVisible(
    By.xpath(s"$portletXpath//ul//p[text()='$title']")
  )

  /** Clicks the "Show all" button.
    */
  def showAll(): NewSearchPage = {
    driver
      .findElement(By.xpath(s"$portletXpath//button[text()='$SHOW_ALL_TEXT']"))
      .click()

    val searchPage = new NewSearchPage(context).get()
    searchPage.waitForSearchCompleted()
    searchPage
  }
}
