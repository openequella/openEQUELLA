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
import org.openqa.selenium.support.ui.ExpectedConditions;

public class HierarchyPage extends AbstractPage<HierarchyPage> {
  private final String hierarchyName;

  private static By loadedElementBy(String hierarchyName) {
    return By.xpath("//h4[text()='" + hierarchyName + "']");
  }

  public HierarchyPage(PageContext context, String hierarchyName) {
    super(context, loadedElementBy(hierarchyName));
    this.hierarchyName = hierarchyName;
  }

  /**
   * Wait for the hierarchy page to load. Since new UI is using async API to render the page, it
   * needs to wait for the page to be loaded.
   */
  public WebElement waitForLoading() {
    return waiter.until(
        ExpectedConditions.presenceOfElementLocated(loadedElementBy(hierarchyName)));
  }
}
