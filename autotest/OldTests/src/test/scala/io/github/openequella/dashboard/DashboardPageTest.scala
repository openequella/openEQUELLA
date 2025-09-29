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
import com.tle.webtests.pageobject.HomePage
import com.tle.webtests.pageobject.portal.MenuSection
import com.tle.webtests.test.AbstractSessionTest
import io.github.openequella.pages.dashboard.DashboardPage
import org.testng.Assert.assertTrue
import org.testng.annotations.Test
import testng.annotation.NewUIOnly

@TestInstitution("fiveo")
class DashboardPageTest extends AbstractSessionTest {

  @NewUIOnly
  @Test(description = "User should be able to access dashboard page from the menu.")
  def accessFromMenu(): Unit = {
    logon()
    val menus = new MenuSection(context).get
    val dashboardPage =
      menus.clickMenu("Dashboard", new DashboardPage(context))
    assertTrue(dashboardPage.isLoaded)
  }
}
