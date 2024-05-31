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
import com.tle.webtests.pageobject.AbstractPage
import org.openqa.selenium.{By, WebElement}

/**
  * Hierarchy panel. Which is a part of the BrowseHierarchiesPage and HierarchyPage.
  */
class HierarchyPanel(context: PageContext) extends AbstractPage[HierarchyPanel](context) {

  /** Get the hierarchy tree element. */
  def getHierarchyTree: WebElement = {
    val hierarchyTree = By.xpath("//ul[@aria-label='View hierarchy']")
    driver.findElement(hierarchyTree)
  }

  /**
    * Click on the hierarchy in the tree view.
    *
    * @param hierarchyName The name of the hierarchy.
    * @param compoundUuid  The compound UUID of the hierarchy.
    */
  def clickHierarchy(hierarchyName: String, compoundUuid: String): HierarchyPage = {
    getHierarchyTree
      .findElement(By.xpath(".//a[contains(text(), '" + hierarchyName + "')]"))
      .click()
    new HierarchyPage(context, hierarchyName, compoundUuid)
  }

  /**
    * Expand the hierarchy tree.
    *
    * @param hierarchyName The name of the hierarchy.
    */
  def expandHierarchy(hierarchyName: String): Unit = {
    val hierarchyLabel = getHierarchyTree.findElement(By.xpath(
      ".//a[contains(text(), '" + hierarchyName + "')]/ancestor::div[contains(@class, 'HierarchyTopic-label')]"))
    val expandButton =
      hierarchyLabel.findElement(By.xpath(".//button[@aria-label='Expand hierarchy']"))
    expandButton.click()
  }

  /**
    * Check if the hierarchy is present in the tree view.
    *
    * @param name The name of the hierarchy.
    */
  def hasHierarchy(name: String): Boolean = {
    val tree = getHierarchyTree
    !tree.findElements(By.xpath(".//a[contains(text(), '" + name + "')]")).isEmpty
  }

  /**
    * Check if the hierarchy tree has the short description.
    *
    * @param shortDesc The short description of the hierarchy.
    */
  def hasHierarchyShortDesc(shortDesc: String): Boolean = {
    val tree = getHierarchyTree
    !tree.findElements(By.xpath(".//div[contains(text(), '" + shortDesc + "')]")).isEmpty
  }

  /**
    * Check if the hierarchy tree has result count.
    *
    * @param hierarchyName The name of the hierarchy.
    */
  def hasHierarchyResultCount(hierarchyName: String): Boolean = {
    val tree = getHierarchyTree
    !tree
      .findElements(
        By.xpath(".//a[contains(text(), '" + hierarchyName + "')]/following-sibling::span"))
      .isEmpty
  }

  /**
    * Check if the hierarchy tree has result count.
    *
    * @param hierarchyName The name of the hierarchy.
    * @param count         The result count number of the hierarchy.
    */
  def isResultCountShowed(hierarchyName: String, count: Int): Boolean = {
    val tree = getHierarchyTree
    !tree
      .findElements(By.xpath(
        ".//a[contains(text(), '" + hierarchyName + "')]/following-sibling::span[contains(., '(" + count + ")')]"))
      .isEmpty
  }
}
