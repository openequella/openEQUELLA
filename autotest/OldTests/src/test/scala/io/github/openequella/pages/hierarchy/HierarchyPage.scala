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

package io.github.openequella.pages.hierarchy

import com.tle.webtests.framework.PageContext
import com.tle.webtests.pageobject.{ExpectedConditions2, PageObject, WaitingPageObject}
import io.github.openequella.pages.search.AbstractSearchPage
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.{By, JavascriptExecutor, WebElement}

class HierarchyPage(context: PageContext,
                    val hierarchyName: String,
                    val hierarchyCompoundUuid: String)
    extends AbstractSearchPage[HierarchyPage](context) {
  loadedBy = By.xpath("//h4[text()='" + hierarchyName + "']")
  val hierarchyPanel = new HierarchyPanel(context)

  val addKeyResourceLabel    = "Add as a key resource"
  val removeKeyResourceLabel = "Remove key resource"

  /**
    * Wait for the hierarchy page to load. Since new UI is using async API to render the page, it
    * needs to wait for the page to be loaded.
    */
  override def findLoadedElement: WebElement =
    waiter.until(ExpectedConditions.presenceOfElementLocated(loadedBy))

  override def loadUrl(): Unit =
    driver.get(context.getBaseUrl + "page/hierarchy/" + hierarchyCompoundUuid)

  /**
    * Wait until the correct number of items are displayed.
    *
    * @param itemCount The expected number of items.
    */
  override def waitForSearchCompleted(itemCount: Int): Unit = {
    waiter.until(
      ExpectedConditions.visibilityOfElementLocated(
        By.xpath("//span[contains(text()='(" + itemCount + ")')]")))
  }

  //  Wait until the correct number of key resources are displayed.
  private def waitForKeyResourceUpdated(keyResourceCount: Int): Unit = {
    val keyResource = By.xpath("//div[contains(@class, 'KeyResource-container')]")
    waiter.until(
      ExpectedConditions2.numberOfElementLocated(context.getDriver, keyResource, keyResourceCount))
  }

  /** Get the key resource panel element. */
  def getKeyResourcePanel: WebElement = {
    val keyResourcePanel = By.xpath("//div[@data-testid='key-resource-panel']")
    driver.findElement(keyResourcePanel)
  }

  def getSubtopicSectionName: String = {
    val sectionName = By.xpath("//h4[text()='" + hierarchyName + "']/following-sibling::h5")
    driver.findElement(sectionName).getText
  }

  // By XPath to find the pin icon button on search result list.
  private def pinIconXpath(itemName: String, pinLabel: String): By =
    By.xpath(
      ".//a[text()='" + itemName + "']/../following-sibling::section//button[@aria-label='" + pinLabel + "']")

  // Select the version of the key resource in the dialog
  private def selectKeyResourceVersion(isLatest: Boolean): Unit = {
    val selectVersionDialog = driver.findElement(By.xpath("//div[@role='dialog']"))
    val versionLabelXpath =
      if (isLatest) By.xpath("//span[contains(text(), 'Always use latest version')]")
      else By.xpath("//span[contains(text(), 'This version')]");
    selectVersionDialog.findElement(versionLabelXpath).click()
  }

  // Confirm the select version dialog.
  private def confirmDialog(): Unit = {
    val selectVersionDialog = driver.findElement(By.xpath("//div[@role='dialog']"))
    val confirmButton       = selectVersionDialog.findElement(By.id("confirm-dialog-confirm-button"))
    waiter.until(ExpectedConditions.elementToBeClickable(confirmButton))
    confirmButton.click()
  }

  /**
    * When the pin icon is highlighted click to remove this item from key resource.
    *
    * @param itemName The name of the item to add as a key resource.
    */
  def addKeyResourceFromResultList(itemName: String): Unit = {
    val originalResourceCount = keyResourceCount
    val addButton             = getSearchList.findElement(pinIconXpath(itemName, addKeyResourceLabel))
    addButton.click()

    selectKeyResourceVersion(isLatest = false);
    confirmDialog();

    waitForKeyResourceUpdated(keyResourceCount = originalResourceCount + 1)
  }

  /**
    * When the pin icon is highlighted click to remove this item from key resource.
    *
    * @param itemName The name of the item to remove from key resource.
    */
  def removeKeyResourceFromSearchResult(itemName: String): Unit = {
    val originalResourceCount = keyResourceCount
    val button                = getSearchList.findElement(pinIconXpath(itemName, removeKeyResourceLabel))
    waiter.until(ExpectedConditions.elementToBeClickable(button))
    button.click()

    confirmDialog()

    waitForKeyResourceUpdated(keyResourceCount = originalResourceCount - 1)
  }

  /**
    * Click the pin icon button in KeyResourcePanel to remove the key resource.
    *
    * @param itemName The name of the item to remove from the key resource panel.
    */
  def removeKeyResourceFromKeyResourcePanel(itemName: String): Unit = {
    val originalResourceCount = keyResourceCount
    val pinIconXpath = By.xpath(
      ".//a[text()='" + itemName + "']/ancestor::div[contains(@class, 'KeyResource-container')]//button[@aria-label='" + removeKeyResourceLabel + "']")

    val button = getKeyResourcePanel.findElement(pinIconXpath)
    driver.asInstanceOf[JavascriptExecutor].executeScript("arguments[0].click();", button)

    confirmDialog()

    waitForKeyResourceUpdated(keyResourceCount = originalResourceCount - 1)
  }

  /**
    * Check if the key resource panel has the provided item.
    *
    * @param itemName The name of the item to check.
    */
  def hasKeyResource(itemName: String): Boolean = {
    val keyResourceItemXpath =
      By.xpath("//div[@data-testid='key-resource-panel']//a[text()='" + itemName + "']")
    !driver.findElements(keyResourceItemXpath).isEmpty
  }

  /**
    * Get the number of key resources in the key resource panel.
    */
  def keyResourceCount: Int = {
    val keyResourceItemXpath = By.xpath("//div[contains(@class, 'KeyResource-container')]")
    driver.findElements(keyResourceItemXpath).size()
  }

  /**
    * Check if the pin icon is highlighted for the provided item.
    *
    * @param itemName The name of the item to check.
    */
  def isItemPinIconHighlighted(itemName: String): Boolean = {
    val removePinIconXpath = By.xpath(
      ".//a[text()='" + itemName + "']/../following-sibling::section//button[@aria-label='" + removeKeyResourceLabel + "']")
    val removePinIcon = getSearchList.findElements(removePinIconXpath)

    !removePinIcon.isEmpty
  }

  /**
    * Get the number of breadcrumbs.
    */
  def breadcrumbCount: Int =
    driver
      .findElements(By.xpath("//nav[@aria-label='breadcrumb']//li[@class='MuiBreadcrumbs-li']"))
      .size()

  /**
    * Click the breadcrumb to navigate to the hierarchy.
    *
    * @param hierarchyName The name of the hierarchy to click.
    * @param returnTo The page object to return to after clicking.
    */
  def clickBreadcrumb[T <: PageObject](hierarchyName: String, returnTo: WaitingPageObject[T]): T = {
    val breadcrumbs = driver.findElement(By.xpath("//nav[@aria-label='breadcrumb']"))
    val breadcrumb  = breadcrumbs.findElement(By.xpath(".//a[text()='" + hierarchyName + "']"))
    breadcrumb.click()
    returnTo.get
  }
}
