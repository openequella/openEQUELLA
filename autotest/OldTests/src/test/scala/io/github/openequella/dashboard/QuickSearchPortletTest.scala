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

package io.github.openequella.dashboard

import com.tle.webtests.framework.TestInstitution
import io.github.openequella.pages.dashboard.PortletType
import io.github.openequella.pages.dashboard.portlets.{PortletFactory, QuickSearchPortlet}
import org.testng.Assert.{assertEquals, assertTrue}
import org.testng.annotations.{BeforeClass, BeforeMethod, Test}
import testng.annotation.NewUIOnly

@NewUIOnly
@TestInstitution("vanilla")
class QuickSearchPortletTest extends AbstractPortletTest {
  val itemName                               = "exists"
  var quickSearchPortletName: String         = _
  var quickSearchPortlet: QuickSearchPortlet = _

  @BeforeClass
  def setupClass(): Unit = {
    quickSearchPortletName = context.getFullName("Quick search")

    loginWithPortletAccount()
    loadDashboardPage()
    dashboardPage.createPortlet(PortletType.QuickSearch, quickSearchPortletName)
  }

  @BeforeMethod
  def setupQuickSearchPortletTest(): Unit = {
    quickSearchPortlet =
      dashboardPage.getPortlet(PortletFactory.QuickSearch, quickSearchPortletName)
  }

  @Test(description = "Should be redirect to search page")
  def showAll(): Unit = {
    val searchPage = quickSearchPortlet.showAll()
    assertTrue(searchPage.isLoaded)
  }

  @Test(description = "Should be able to see result")
  def showResults(): Unit = {
    quickSearchPortlet.search(itemName)
    assertTrue(quickSearchPortlet.hasResult(itemName))
  }

  @Test(description = "Should be redirect to search page with query")
  def showAllWithQuery(): Unit = {
    quickSearchPortlet.search(itemName)
    val searchPage = quickSearchPortlet.showAll()

    assertTrue(searchPage.isLoaded)
    assertEquals(searchPage.getQuery, itemName)
  }
}
