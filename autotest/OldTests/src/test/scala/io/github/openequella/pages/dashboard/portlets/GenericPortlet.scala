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
import com.tle.webtests.pageobject.{AbstractPage, PageObject}
import io.github.openequella.pages.dashboard.EditPortletPage
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.{By, WebElement}

/** Represents a basic Portlet object. Include general portlet operations like minimise, maximise,
  * delete, close.
  *
  * @param context
  *   The PageContext for the current test session.
  * @param name
  *   The name of the Portlet.
  */
class GenericPortlet[T <: PageObject](context: PageContext, name: String)
    extends AbstractPage[T](context) {
  val portletXpath = s"//div[contains(@id, 'portlet')]//span[text()='$name']/ancestor::div[3]"
  val contentContainerBy: By = By.xpath(s"$portletXpath//div[contains(@id, 'portlet-content-')]")

  /** The XPath locator used to validate the portlet type.
    */
  var validationXpath: String = portletXpath

  loadedBy = By.xpath(portletXpath)

  /** Waits for the Portlet content to be loaded.
    */
  def waitForLoad(): Unit = {
    if (!isMinimised) {
      waiter.until(ExpectedConditions.visibilityOfElementLocated(contentContainerBy))
    }
  }

  /** Validate the portlet type by checking for the presence of a specific element within the
    * portlet.
    */
  def validate(): Boolean = {
    waitForLoad()
    val originalMinimisedState = isMinimised

    // Ensure the portlet is maximised for validation.
    if (originalMinimisedState) {
      maximise()
      waitForLoad()
    }

    val isValid = isVisible(By.xpath(validationXpath))

    // Restore the original minimised state.
    if (originalMinimisedState) {
      minimise()
    }

    isValid
  }

  /** Check is the Portlet is minimised.
    */
  def isMinimised: Boolean =
    !isPresent(contentContainerBy) && isPresent(getPortletButtonBy("Maximise"))

  /** Clicks the minimise button of a portlet with the given title.
    */
  def minimise(): Unit = getPortletButton("Minimise").click()

  /** Clicks the maximise button of a portlet with the given title.
    */
  def maximise(): Unit = getPortletButton("Maximise").click()

  /** Clicks the edit button.
    */
  def edit(): EditPortletPage = {
    getPortletButton("Edit").click()
    new EditPortletPage(context).get()
  }

  /** Clicks the delete button.
    */
  def delete(): Unit = {
    getPortletButton("Delete").click()
    confirmDialog()
  }

  /** Clicks the close button.
    */
  def close(): Unit = {
    getPortletButton("Close").click()
    confirmDialog()
  }

  private def getPortletButton(buttonLabel: String): WebElement = {
    val buttonBy = getPortletButtonBy(buttonLabel)
    driver.findElement(buttonBy)
  }

  private def getPortletButtonBy(buttonLabel: String): By =
    By.xpath(s"$portletXpath//button[@aria-label='$buttonLabel']")
}
