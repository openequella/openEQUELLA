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
import com.tle.webtests.pageobject.portal.{DashboardAdminPage, MenuSection}
import com.tle.webtests.test.{AbstractCleanupTest, AbstractSessionTest}
import io.github.openequella.pages.dashboard.DashboardLayout.{SingleColumn, TwoEqualColumn}
import io.github.openequella.pages.dashboard.portlets.PortletFactory.Generic
import io.github.openequella.pages.dashboard.{DashboardPage, PortletType}
import org.testng.Assert.{assertEquals, assertFalse, assertTrue}
import org.testng.annotations.{AfterMethod, BeforeMethod, Test}
import testng.annotation.NewUIOnly

@NewUIOnly
@TestInstitution("fiveo")
class DashboardPageTest extends AbstractCleanupTest {
  val favouritesPortletTitle = "Favourites Portlet"
  val globalPortletTitle     = "Test global portlet"

  var dashboardPage: DashboardPage = _

  def DashboardPageTest(): Unit = {
    setDeleteCredentials(AbstractSessionTest.AUTOTEST_LOGON, AbstractSessionTest.AUTOTEST_PASSWD)
  }

  @BeforeMethod
  def setupTest(): Unit = {
    logon()
    dashboardPage = new DashboardPage(context)
    dashboardPage.waitForLoad()
  }

  @AfterMethod
  def cleanupTest(): Unit = {
    logon()
    dashboardPage = new DashboardPage(context)
    dashboardPage.waitForLoad()
    dashboardPage.changeLayout(SingleColumn)
    cleanupPortlets()
  }

  @Override
  override def cleanupAfterClass(): Unit = {
    logon()
    // Delete all portlets created by this test class (the ones with the name prefix).
    cleanupPortlets()
    super.cleanupAfterClass()
  }

  @Test(description = "User should be able to access dashboard page from the menu.")
  def accessFromMenu(): Unit = {
    val menus = new MenuSection(context).get
    val dashboardPage =
      menus.clickMenu("Dashboard", new DashboardPage(context))

    assertTrue(dashboardPage.isLoaded)
  }

  @Test(description = "User should be able to add a portlet.")
  def addPortlet(): Unit = {
    val portletName = createPortlet()

    assertTrue(dashboardPage.hasPortlet(portletName))
  }

  @Test(
    description = "User should be able to edit a portlet."
  )
  def editPortlet(): Unit = {
    val portletName     = createPortlet()
    val editPortletPage = dashboardPage.editPortlet(portletName)

    assertTrue(editPortletPage.isLoaded)
  }

  @Test(
    description = "User should be able to minimised/maximise a portlet."
  )
  def minimiseAndMaximisePortlet(): Unit = {
    val portletName = createPortlet()

    val portlet = dashboardPage.getPortlet(Generic, portletName)
    assertFalse(portlet.isMinimised)

    portlet.minimise()
    assertTrue(portlet.isMinimised)

    portlet.maximise()
    assertFalse(portlet.isMinimised)
  }

  @Test(
    description = "User should be able to delete a portlet."
  )
  def deletePortlet(): Unit = {
    val portletName = createPortlet()
    assertTrue(dashboardPage.hasPortlet(portletName))

    dashboardPage.deletePortlet(portletName)
    assertFalse(dashboardPage.hasPortlet(portletName))
  }

  @Test(description = "User should be able to close a portlet.")
  def closePortlet(): Unit = {
    dashboardPage.closePortlet(globalPortletTitle)

    assertFalse(dashboardPage.hasPortlet(globalPortletTitle))
  }

  @Test(
    description = "User should be able to restore a portlet.",
    dependsOnMethods = Array("closePortlet")
  )
  def restorePortlet(): Unit = {
    dashboardPage.restorePortlet(globalPortletTitle)

    assertTrue(dashboardPage.hasPortlet(globalPortletTitle))
  }

  @Test(
    description = "User should be able to change layout."
  )
  def changeLayout(): Unit = {
    dashboardPage.changeLayout(TwoEqualColumn)

    assertTrue(dashboardPage.inTwoColumnsLayout)
  }

  @Test(
    description = "User should be able to drag and drop the portlet.",
    dependsOnMethods = Array("restorePortlet")
  )
  def dragAndDropPortlet(): Unit = {
    val test1PortletTitle = context.getFullName("Test Portlet 1")
    val test2PortletTitle = context.getFullName("Test Portlet 2")

    // The order of portlets:
    //    ──────────────────────────────────────────────────────────
    //    Column 0(left)                        Column 1(right)
    //    ──────────────────────────────────────────────────────────
    //    order 0: [test1PortletTitle]     │
    //    order 1: [test2PortletTitle]     │
    //    order 2: [globalPortletTitle]    │
    //    ──────────────────────────────────────────────────────────
    dashboardPage.changeLayout(TwoEqualColumn)
    dashboardPage.createPortlet(PortletType.Browse, test1PortletTitle)
    dashboardPage.createPortlet(PortletType.Browse, test2PortletTitle)

    // Minimise all portlets to make sure the following drag and drop actions work
    // (to avoid moveOutOfBound issue, because portlets may not be in the viewport).
    dashboardPage.minimisePortlet(test1PortletTitle)
    dashboardPage.minimisePortlet(test2PortletTitle)
    dashboardPage.minimisePortlet(globalPortletTitle)

    dashboardPage.dragAndDropPortletAbove(test1PortletTitle, test2PortletTitle);
    // Should not change anything.
    assertEquals(
      dashboardPage.getPortletTitles,
      List(test1PortletTitle, test2PortletTitle, globalPortletTitle)
    )

    // Move top portlet to second, expect the order to be:
    //    ─────────────────────────────────────────────────────────────────
    //    Column 0(left)                      Column 1(right)
    //    ─────────────────────────────────────────────────────────────────
    //    order 0: [test2PortletTitle]     │
    //    order 1: [test1PortletTitle]     │
    //    order 2: [globalPortletTitle]    │
    //    ─────────────────────────────────────────────────────────────────
    dashboardPage.dragAndDropPortletBelow(test1PortletTitle, test2PortletTitle);
    assertEquals(
      dashboardPage.getPortletTitles,
      List(test2PortletTitle, test1PortletTitle, globalPortletTitle)
    )

    // Move top portlet to bottom, expect the order to be:
    //    ─────────────────────────────────────────────────────────────────
    //    Column 0(left)                      Column 1(right)
    //    ─────────────────────────────────────────────────────────────────
    //    order 0: [test1PortletTitle]     │
    //    order 1: [globalPortletTitle]    │
    //    order 2: [test2PortletTitle]     │
    //    ─────────────────────────────────────────────────────────────────
    dashboardPage.dragAndDropPortletBelow(test2PortletTitle, globalPortletTitle);
    assertEquals(
      dashboardPage.getPortletTitles,
      List(test1PortletTitle, globalPortletTitle, test2PortletTitle)
    )

    // Move the global portlet to the right column, expect the order to be:.
    //    ─────────────────────────────────────────────────────────────────
    //    Column 0(left)                      Column 1(right)
    //    ─────────────────────────────────────────────────────────────────
    //    order 0: [test1PortletTitle]     │  order 0: [globalPortletTitle]
    //    order 1: [test2PortletTitle]     │
    //    ─────────────────────────────────────────────────────────────────
    dashboardPage.dragAndDropPortlet(globalPortletTitle, dashboardPage.getRightColumn);
    assertTrue(dashboardPage.isPortletInRightColumn(globalPortletTitle))
    assertEquals(
      dashboardPage.getPortletTitles,
      List(test1PortletTitle, test2PortletTitle, globalPortletTitle)
    )

    // Move left one to right-top, expect the order to be:
    //    ─────────────────────────────────────────────────────────────────
    //    Column 0(left)                        Column 1(right)
    //    ─────────────────────────────────────────────────────────────────
    //    order 0: [test2PortletTitle]    │  order 0: [test1PortletTitle]
    //                                    │  order 1: [globalPortletTitle]
    //    ─────────────────────────────────────────────────────────────────
    dashboardPage.dragAndDropPortletAbove(test1PortletTitle, globalPortletTitle);
    assertTrue(dashboardPage.isPortletInRightColumn(test1PortletTitle))
    assertTrue(dashboardPage.isPortletInRightColumn(globalPortletTitle))
    assertEquals(
      dashboardPage.getPortletTitles,
      List(test2PortletTitle, test1PortletTitle, globalPortletTitle)
    )

    // Move left one to right-bottom, expect the order to be:
    //    ─────────────────────────────────────────────────────────────────
    //    Column 0(left)                        Column 1(right)
    //    ─────────────────────────────────────────────────────────────────
    //                                    │  order 0: [test1PortletTitle]
    //                                    │  order 1: [globalPortletTitle]
    //                                    │  order 2: [test2PortletTitle]
    //    ─────────────────────────────────────────────────────────────────
    dashboardPage.dragAndDropPortletBelow(test2PortletTitle, globalPortletTitle);
    assertTrue(dashboardPage.isPortletInRightColumn(test1PortletTitle))
    assertTrue(dashboardPage.isPortletInRightColumn(globalPortletTitle))
    assertTrue(dashboardPage.isPortletInRightColumn(test2PortletTitle))
    assertEquals(
      dashboardPage.getPortletTitles,
      List(test1PortletTitle, globalPortletTitle, test2PortletTitle)
    )

    // Move right one to left column, expect the order to be:
    //    ─────────────────────────────────────────────────────────────────
    //    Column 0(left)                        Column 1(right)
    //    ─────────────────────────────────────────────────────────────────
    //    order 0: [test1PortletTitle]    │  order 0: [globalPortletTitle]
    //                                    │  order 1: [test2PortletTitle]
    //    ─────────────────────────────────────────────────────────────────
    dashboardPage.dragAndDropPortlet(test1PortletTitle, dashboardPage.getLeftColumn);
    assertTrue(dashboardPage.isPortletInLeftColumn(test1PortletTitle))
    assertTrue(dashboardPage.isPortletInRightColumn(globalPortletTitle))
    assertTrue(dashboardPage.isPortletInRightColumn(test2PortletTitle))
    assertEquals(
      dashboardPage.getPortletTitles,
      List(test1PortletTitle, globalPortletTitle, test2PortletTitle)
    )

    // Move another right one to left column, expect the order to be:
    //    ─────────────────────────────────────────────────────────────────
    //    Column 0(left)                        Column 1(right)
    //    ─────────────────────────────────────────────────────────────────
    //    order 0: [test1PortletTitle]    │  order 0: [test2PortletTitle]
    //    order 1: [globalPortletTitle]   │
    //    ─────────────────────────────────────────────────────────────────
    dashboardPage.dragAndDropPortlet(globalPortletTitle, dashboardPage.getLeftColumn);
    assertTrue(dashboardPage.isPortletInLeftColumn(test1PortletTitle))
    assertTrue(dashboardPage.isPortletInLeftColumn(globalPortletTitle))
    assertTrue(dashboardPage.isPortletInRightColumn(test2PortletTitle))
    assertEquals(
      dashboardPage.getPortletTitles,
      List(test1PortletTitle, globalPortletTitle, test2PortletTitle)
    )

    // Move another left one to right column, expect the order to be:
    //    ─────────────────────────────────────────────────────────────────
    //    Column 0(left)                        Column 1(right)
    //    ─────────────────────────────────────────────────────────────────
    //    order 0: [globalPortletTitle]   │  order 0: [test2PortletTitle]
    //                                    │  order 1: [test1PortletTitle]
    //    ─────────────────────────────────────────────────────────────────
    dashboardPage.dragAndDropPortlet(test1PortletTitle, dashboardPage.getRightColumn);
    assertTrue(dashboardPage.isPortletInLeftColumn(globalPortletTitle))
    assertTrue(dashboardPage.isPortletInRightColumn(test2PortletTitle))
    assertTrue(dashboardPage.isPortletInRightColumn(test1PortletTitle))
    assertEquals(
      dashboardPage.getPortletTitles,
      List(globalPortletTitle, test2PortletTitle, test1PortletTitle)
    )

    // Restore the global portlet.
    dashboardPage.maximisePortlet(globalPortletTitle)
  }

  // Set up a favourites portlet for testing.
  private def createPortlet(): String = {
    val portletName = context.getFullName("Favourites Portlet")
    dashboardPage.createPortlet(PortletType.Favourites, portletName)
    portletName
  }

  // Clean up all portlets created by test.
  private def cleanupPortlets(): Unit = {
    val namePrefix = context.getNamePrefix
    new DashboardAdminPage(context).load().deleteAllPortlet(namePrefix)
  }
}
