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

package io.github.openequella.pages.oidc

import com.tle.webtests.framework.PageContext
import com.tle.webtests.pageobject.AbstractPage
import io.github.openequella.pages.components.{SelectCustomRolesDialog, SelectRolesDialog}
import org.openqa.selenium.interactions.Actions
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.{By, Keys, WebElement}

class OidcSettingsPage(context: PageContext) extends AbstractPage[OidcSettingsPage](context) {
  // Title of the page.
  loadedBy = By.xpath("//h2[text()='General Details']");
  // The spinner when the settings are loading.
  val spinnerBy: By = By.xpath("//circle[contains(@class, 'MuiCircularProgress-circle')]");
  // The message when the settings are saved successfully.
  val savedMessageBy: By = By.xpath("//span[contains(., 'Saved successfully.')]");
  // The OIDC enabled switch button.
  val oidcEnabledBy: By = By.xpath(
    "//span[contains(.,'Enable *')]/ancestor::li//input/parent::span"
  );
  val idpSelectBy: By = By.xpath("//div[@aria-label='Select Identity Provider']/div");

  override def findLoadedElement: WebElement = {
    waiter.until(ExpectedConditions.invisibilityOfElementLocated(spinnerBy))
    waiter.until(ExpectedConditions.presenceOfElementLocated(loadedBy))
  }

  override def loadUrl(): Unit =
    driver.get(context.getBaseUrl + "page/oidc")

  /** Get the xpath of the setting by the label.
    */
  private def getSettingXpath(label: String): String =
    s"//span[contains(.,'$label')]/ancestor::li"

  /** Check if the OIDC is enabled.
    */
  def isOidcEnabled: Boolean = {
    val enableButton = driver.findElement(
      oidcEnabledBy
    )
    enableButton.getAttribute("class").contains("Mui-checked")
  }

  /** Click the switch button to enable or disable OIDC. */
  def enableOidc(enable: Boolean): Unit = {
    if (enable != isOidcEnabled) {
      val enableButton = driver.findElement(
        oidcEnabledBy
      )
      enableButton.click()
    }
  }

  // Get the text field element by the label.
  private def getTextField(label: String): WebElement =
    driver.findElement(
      By.xpath(
        s"${getSettingXpath(label)}//input"
      )
    )

  /** Input text filed type of OIDC setting.
    *
    * @param label
    *   the label of the setting.
    * @param value
    *   the value of the setting.
    */
  def inputTextField(label: String, value: String): Unit = {
    val textInput = waiter.until(
      ExpectedConditions.elementToBeClickable(
        getTextField(label)
      )
    )
    clearText(textInput)
    textInput.sendKeys(value)
  }

  /** Get the value of the text field.
    *
    * @param label
    *   the label of the text field.
    */
  def getTextFieldValue(label: String): String = getTextField(label).getAttribute("value")

  /** Select the identity provider.
    */
  def selectIdP(idp: String): Unit = {
    val idpSelect = driver.findElement(idpSelectBy)
    idpSelect.click()
    val idpOption = waiter.until(
      ExpectedConditions.presenceOfElementLocated(
        By.xpath(s"//li[@role='option' and contains(., '$idp')]")
      )
    )
    idpOption.click()
  }

  /** Get the value of the identity provider.
    */
  def getIdpValue: String = driver.findElement(idpSelectBy).getText

  private def openRoleDialog(label: String): Unit = {
    val editIcon = driver.findElement(By.xpath(s"//button[@aria-label='$label']"))
    new Actions(driver).moveToElement(editIcon).perform()
    editIcon.click()
  }

  def openDefaultRoleDialog(): Unit = openRoleDialog("Edit Default roles")

  def openCustomRoleDialog(): Unit = openRoleDialog("Edit Custom roles")

  /** Open the select roles dialog and select the roles.
    *
    * @param roles
    *   the names of the roles to select.
    */
  def selectDefaultRoles(roles: List[String]): Unit = {
    openDefaultRoleDialog()

    val roleDialog = new SelectRolesDialog(context)
    roleDialog.searchRoles(None)
    roles.foreach(roleDialog.selectRole)
    roleDialog.ok()
  }

  private def getBadgeNumber(settingsLabel: String): Int = {
    val badge = waiter.until(
      ExpectedConditions.presenceOfElementLocated(
        By.xpath(s"${getSettingXpath(settingsLabel)}//span[contains(@class, 'MuiBadge-badge')]")
      )
    )
    badge.getText.toInt
  }

  /** Get the number of selected default roles.
    */
  def getDefaultRolesNumber: Int = getBadgeNumber("Default roles")

  /** Get the number of selected default roles.
    */
  def getCustomRolesNumber: Int = getBadgeNumber("Custom roles")

  /** Open the select custom roles dialog and select the roles.
    *
    * @param roles
    *   the custom roles and oeq roles to select.
    */
  def selectCustomRoles(roles: Map[String, List[String]]): Unit = {
    openCustomRoleDialog()

    val customRoleDialog = new SelectCustomRolesDialog(context)

    customRoleDialog.searchRoles(None)
    roles.foreach { case (customRole, oeqRoles) =>
      customRoleDialog.inputCustomRole(customRole)
      oeqRoles.foreach(oeqRole => customRoleDialog.selectRole(oeqRole))
    }

    customRoleDialog.ok()
  }

  def deleteCustomRoles(roles: Map[String, List[String]]): Unit = {
    openCustomRoleDialog()

    val customRoleDialog = new SelectCustomRolesDialog(context)
    roles.foreach({ case (customRole, oeqRoles) =>
      oeqRoles.foreach(oeqRole => customRoleDialog.deleteRole(customRole, oeqRole))
    })

    customRoleDialog.ok()
  }

  /** Click the save button to save the OIDC settings.
    */
  def save(): Unit = {
    val saveButton = waiter.until(
      ExpectedConditions.elementToBeClickable(By.xpath("//button[@aria-label='Save']"))
    )
    // Move to save button area before clicking to avoid intercepted error.
    new Actions(driver).moveToElement(saveButton).perform()
    saveButton.click()
    waiter.until(ExpectedConditions.visibilityOfElementLocated(savedMessageBy))
  }
}
