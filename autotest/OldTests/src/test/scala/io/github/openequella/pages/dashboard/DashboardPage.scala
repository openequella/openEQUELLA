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

package io.github.openequella.pages.dashboard

import com.tle.webtests.framework.PageContext
import com.tle.webtests.pageobject.AbstractPage
import io.github.openequella.pages.dashboard.DashboardLayout.DashboardLayout
import io.github.openequella.pages.dashboard.PortletType.PortletType
import io.github.openequella.pages.dashboard.portlets.{GenericPortlet, PortletFactory}
import org.openqa.selenium.interactions.Actions
import org.openqa.selenium.support.ui.{ExpectedCondition, ExpectedConditions}
import org.openqa.selenium.{By, WebDriver, WebElement}

import scala.jdk.CollectionConverters._

/** Top level component for Dashboard, which display either all the viewable portlets or a Welcome
  * board.
  */
class DashboardPage(
    context: PageContext
) extends AbstractPage[DashboardPage](context) {
  // Title of the page.
  loadedBy = By.xpath("//h5[text()='Dashboard']")

  val editPanelTitleBy: By = By.xpath("//h5[text()='Dashboard editor']")

  val leftColumnXpath  = "//div[@id='portlet-container-left-column']"
  val rightColumnXpath = "//div[@id='portlet-container-right-column']"

  override def loadUrl(): Unit =
    driver.get(context.getBaseUrl + "page/home")

  /** Waits for either the Welcome board or the portlets to be loaded.
    */
  def waitForLoad(): Unit = waiter.until(new ExpectedCondition[Boolean] {
    override def apply(driver: WebDriver): Boolean = {
      lazy val welcome =
        !driver.findElements(By.xpath("//*[text()='Welcome to openEQUELLA']")).isEmpty

      lazy val portlets =
        !driver.findElements(By.cssSelector("#dashboard-portlet-container")).isEmpty

      welcome || portlets
    }
  })

  /** Change the layout of the dashboard to the given layout name.
    */
  def changeLayout(layout: DashboardLayout): Unit = {
    openEditPanel()
    val layoutButton = driver.findElement(
      By.xpath(s"//div[@role='presentation']//button[@aria-label='$layout']")
    )
    layoutButton.click()
    closeEditPanel()
  }

  /** Create a portlet of given type with the given title.
    *
    * @param portletType
    *   The type of portlet to create.
    * @param title
    *   The title of the portlet to create.
    */
  def createPortlet(portletType: PortletType, title: String): Unit = {
    openCreatePortletPage(portletType)

    val createPortletPage = new CreatePortletPage(context).get()
    createPortletPage.setTitle(title)
    createPortletPage.save()
  }

  /** Click the create portlet button for the given portlet type.
    *
    * @param portletType
    *   The type of portlet to create.
    */
  def openCreatePortletPage(portletType: PortletType): Unit = {
    openEditPanel()
    clickEditPortletTab("Create portlet")
    clickCreatePortletButton(portletType)
  }

  /** Restores a closed portlet with the given title.
    */
  def restorePortlet(title: String): Unit = {
    openEditPanel()
    clickEditPortletTab("Restore portlet")

    // Use waitForElement since it need wait for the response from server to show the closed portlet list.
    val restoreButton = waitForElement(
      By.xpath(
        s"//div[@role='presentation']//span[text()='$title']/ancestor::li[1]//button[@aria-label='Restore']"
      )
    )
    restoreButton.click()
    closeEditPanel()
  }

  /** Checks if a portlet with the given title exists on the dashboard.
    *
    * @param title
    *   The title of the portlet to check for.
    */
  def hasPortlet(title: String): Boolean = {
    val portletTitleLocator = By.xpath(getPortletTitleXpath(title))
    isPresent(portletTitleLocator)
  }

  /** Clicks the edit button of a portlet with the given title.
    */
  def editPortlet(title: String): EditPortletPage = getPortlet(PortletFactory.Generic, title).edit()

  /** Clicks the delete button of a portlet with the given title.
    */
  def deletePortlet(title: String): Unit = getPortlet(PortletFactory.Generic, title).delete()

  /** Clicks the close button of a portlet with the given title.
    */
  def closePortlet(title: String): Unit = getPortlet(PortletFactory.Generic, title).close()

  /** Clicks the minimise button of a portlet with the given title.
    */
  def minimisePortlet(title: String): Unit = getPortlet(PortletFactory.Generic, title).minimise()

  /** Clicks the maximise button of a portlet with the given title.
    */
  def maximisePortlet(title: String): Unit = getPortlet(PortletFactory.Generic, title).maximise()

  /** Drags and drops a portlet with the given title to the given WebElement.
    *
    * @param portlet
    *   The title of the portlet to be dragged.
    * @param target
    *   The target WebElement to drop onto.
    */
  def dragAndDropPortlet(portlet: String, target: WebElement): Unit =
    dragAndDropPortlet(portlet, target, 0)

  /** Drags and drops a portlet with the given title above another portlet with the given title.
    *
    * @param portlet
    *   The title of the portlet to be dragged.
    * @param targetPortlet
    *   The title of the target portlet to drop onto.
    */
  def dragAndDropPortletAbove(
      portlet: String,
      targetPortlet: String
  ): Unit = {
    val toPortlet = getPortlet(targetPortlet)
    dragAndDropPortlet(portlet, toPortlet, 0)
  }

  /** Drags and drops a portlet with the given title below another portlet with the given title.
    */
  def dragAndDropPortletBelow(portlet: String, targetPortlet: String): Unit = {
    val toPortlet = getPortlet(targetPortlet)
    // Set Y position to half of the height of the target portlet to drop below it (the default drop point is center of the portlet).
    val offsetY = toPortlet.getSize.getHeight / 2

    dragAndDropPortlet(portlet, toPortlet, offsetY)
  }

  private def dragAndDropPortlet(portlet: String, target: WebElement, offsetY: Int): Unit = {
    val portletHeader = getPortletHeader(portlet)
    new Actions(driver)
      .clickAndHold(portletHeader)
      .moveToElement(target, 0, offsetY)
      .release()
      .perform()
  }

  /** Checks if a portlet with the given title is in the left column.
    *
    * @param portlet
    *   The title of the portlet to check.
    */
  def isPortletInLeftColumn(portlet: String): Boolean = isPresent(
    By.xpath(s"$leftColumnXpath${getPortletTitleXpath(portlet)}")
  )

  /** Checks if a portlet with the given title is in the right column.
    *
    * @param portlet
    *   The title of the portlet to check.
    */
  def isPortletInRightColumn(portlet: String): Boolean = isPresent(
    By.xpath(s"$rightColumnXpath${getPortletTitleXpath(portlet)}")
  )

  /** Gets the titles of all portlets on the dashboard by their displayed order. For example, give
    * this layout:
    *
    * {{{
    *    ────────────────────────────────────────────────────────────
    *    Column 0(left)                    Column 1(right)
    *    ────────────────────────────────────────────────────────────
    *    order 0: [basicPortlet]     │  order 0: [minimisedPortlet]
    *    order 1: [privatePortlet]   │  order 1: [noEditPortlet]
    *    ────────────────────────────────────────────────────────────
    * }}}
    *
    * The result list will be: List("basicPortlet", "privatePortlet", "minimisedPortlet",
    * "noEditPortlet")
    */
  def getPortletTitles: List[String] = {
    val portlets =
      driver.findElements(By.xpath("//div[contains(@id, 'portlet')]/div/div/div[1]/div/span"))
    portlets.asScala.toList.map(_.getText)
  }

  /** Checks if the dashboard is in two columns layout.
    */
  def inTwoColumnsLayout: Boolean =
    isPresent(By.xpath(leftColumnXpath)) && isPresent(By.xpath(rightColumnXpath))

  /** Gets the left column WebElement.
    */
  def getLeftColumn: WebElement = driver.findElement(By.xpath(leftColumnXpath))

  /** Gets the right column WebElement.
    */
  def getRightColumn: WebElement = driver.findElement(By.xpath(rightColumnXpath))

  /** Get the portlet of the specified type with title.
    *
    * @param factory
    *   The PortletFactory to create the portlet.
    * @param title
    *   The title of the portlet.
    */
  def getPortlet[P <: GenericPortlet[_]](factory: PortletFactory[P], title: String): P = {
    val portlet = factory.create(context, title)
    if (portlet.validate()) {
      return portlet
    }
    throw new IllegalArgumentException(
      s"The portlet with title '$title' is not a valid ${factory.portletType} portlet."
    )
  }

  private def getPortletTitleXpath(title: String): String =
    s"//div[contains(@id, 'portlet')]//span[text()='$title']"

  private def getPortletHeader(title: String): WebElement = {
    val headerBy = By.xpath(getPortletTitleXpath(title))
    driver.findElement(headerBy)
  }

  private def getPortlet(title: String): WebElement = {
    val portletBy = By.xpath(s"${getPortletTitleXpath(title)}/ancestor::div[3]")
    driver.findElement(portletBy)
  }

  private def openEditPanel(): Unit = {
    val editButton = driver.findElement(By.xpath("//button[@aria-label='Edit dashboard']"))
    editButton.click()
    waiter.until(ExpectedConditions.presenceOfElementLocated(editPanelTitleBy))
  }

  private def closeEditPanel(): Unit = {
    val closeButton =
      driver.findElement(By.xpath("//div[@role='presentation']//button[@aria-label='Close']"))
    closeButton.click()
    waiter.until(ExpectedConditions.invisibilityOfElementLocated(editPanelTitleBy))
  }

  private def clickEditPortletTab(tabLabel: String): Unit = {
    val createPortletTab = driver.findElement(By.xpath(s"//button[text()='$tabLabel']"))
    createPortletTab.click()
  }

  private def clickCreatePortletButton(portletType: PortletType): Unit = {
    val createPortletButton =
      driver.findElement(By.xpath(s"//button[@aria-label='Create $portletType portlet']"))
    createPortletButton.click()
  }
}
