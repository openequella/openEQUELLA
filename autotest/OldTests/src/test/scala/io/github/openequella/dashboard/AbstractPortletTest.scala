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

import com.tle.webtests.pageobject.portal.DashboardAdminPage
import com.tle.webtests.test.AbstractCleanupTest
import io.github.openequella.pages.dashboard.DashboardPage
import org.testng.annotations.BeforeMethod

/** Abstract test class for Portlet related tests. It will clear all portlets created during the
  * tests.
  */
abstract class AbstractPortletTest extends AbstractCleanupTest {
  var dashboardPage: DashboardPage = _

  @BeforeMethod
  def setupTest(): Unit = {
    loginWithPortletAccount()
    loadDashboardPage()
  }

  // Cleanup after class to remove any portlets/items created during tests.
  override protected def cleanupAfterClass(): Unit = {
    loginWithPortletAccount()
    val prefix = context.getNamePrefix
    new DashboardAdminPage(context).load.deleteAllPortlet(prefix)
    super.cleanupAfterClass()
  }

  protected def loginWithPortletAccount(): Unit = logon("portlettest1", "``````")

  protected def loadDashboardPage(): Unit = {
    dashboardPage = new DashboardPage(context)
    dashboardPage.waitForLoad()
  }
}
