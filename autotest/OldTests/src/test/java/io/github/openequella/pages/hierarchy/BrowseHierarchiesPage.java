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

package io.github.openequella.pages.hierarchy;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class BrowseHierarchiesPage extends AbstractPage<BrowseHierarchiesPage> {
  public static final String TITLE = "Browse hierarchies";

  public BrowseHierarchiesPage(PageContext context) {
    super(context, By.xpath("//h5[text()='" + TITLE + "']"));
  }

  @Override
  protected void loadUrl() {
    driver.get(context.getBaseUrl() + "page/hierarchies");
  }

  /** Get the hierarchy tree element. */
  public WebElement getHierarchyTree() {
    By hierarchyTree = By.xpath("//ul[@aria-label='View hierarchy']");
    return driver.findElement(hierarchyTree);
  }

  /**
   * Click on the hierarchy in the tree view.
   *
   * @param hierarchyName The name of the hierarchy.
   */
  public HierarchyPage clickHierarchy(String hierarchyName) {
    getHierarchyTree()
        .findElement(By.xpath(".//a[contains(text(), '" + hierarchyName + "')]"))
        .click();
    return new HierarchyPage(context, hierarchyName);
  }

  /**
   * Expand the hierarchy tree.
   *
   * @param hierarchyName The name of the hierarchy.
   */
  public void expandHierarchy(String hierarchyName) {
    WebElement expandButton =
        getHierarchyTree()
            .findElement(
                By.xpath(
                    ".//a[contains(text(), '"
                        + hierarchyName
                        + "')]/ancestor::div[contains(@class, 'HierarchyTopic-label')]//button[@aria-label='Expand hierarchy']"));
    expandButton.click();
  }

  /**
   * Check if the hierarchy is present in the tree view.
   *
   * @param name The name of the hierarchy.
   */
  public boolean hasHierarchy(String name) {
    WebElement tree = getHierarchyTree();
    return !tree.findElements(By.xpath(".//a[contains(text(), '" + name + "')]")).isEmpty();
  }

  /**
   * Check if the hierarchy tree has the short description.
   *
   * @param shortDesc The short description of the hierarchy.
   */
  public boolean hasHierarchyShortDesc(String shortDesc) {
    WebElement tree = getHierarchyTree();
    return !tree.findElements(By.xpath(".//div[contains(text(), '" + shortDesc + "')]")).isEmpty();
  }

  /**
   * Check if the hierarchy tree has matched count.
   *
   * @param hierarchyName The name of the hierarchy.
   * @param count The count number of the hierarchy.
   */
  public boolean hasHierarchyMatchedCount(String hierarchyName, int count) {
    WebElement tree = getHierarchyTree();

    return !tree.findElements(
            By.xpath(
                ".//a[contains(text(), '"
                    + hierarchyName
                    + "')]/following-sibling::span[contains(., '("
                    + count
                    + ")')]"))
        .isEmpty();
  }
}
