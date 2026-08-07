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
import com.tle.webtests.pageobject.{AbstractPage, PageObject}
import org.openqa.selenium.By

/** Page object representing the abstract portlet settings page in the Dashboard.
  */
abstract class AbstractPortletSettingsPage[T <: PageObject](context: PageContext)
    extends AbstractPage[T](context) {

  /** Sets the title of the portlet.
    */
  def setTitle(title: String): Unit = {
    val titleField = driver.findElement(
      By.xpath(
        "//label[text()='Title']/ancestor::div[contains(@class, 'settingRow')][1]//input[@type='text']"
      )
    )
    titleField.clear()
    titleField.sendKeys(title)
  }

  /** Clicks the save button to save the portlet and returns to the DashboardPage.
    */
  def save(): DashboardPage = {
    // It has a leading space in the button text.
    val saveButton = driver.findElement(By.xpath("//button[text()=' Save']"))
    saveButton.click()
    new DashboardPage(context).get()
  }
}
