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
import io.github.openequella.pages.search.BaseSearchPage
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.{By, WebElement}

class HierarchyPage(context: PageContext,
                    val hierarchyName: String,
                    val hierarchyCompoundUuid: String)
    extends BaseSearchPage[HierarchyPage](context) {
  loadedBy = By.xpath("//h4[text()='" + hierarchyName + "']")
  val hierarchyPanel = new HierarchyPanel(context)

  /**
    * Wait for the hierarchy page to load. Since new UI is using async API to render the page, it
    * needs to wait for the page to be loaded.
    */
  override def findLoadedElement: WebElement =
    waiter.until(ExpectedConditions.presenceOfElementLocated(loadedBy))

  override def loadUrl(): Unit =
    driver.get(context.getBaseUrl + "page/hierarchy/" + hierarchyCompoundUuid)
}
