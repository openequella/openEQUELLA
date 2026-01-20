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

package io.github.openequella.hierarchy

import com.tle.webtests.framework.TestInstitution
import com.tle.webtests.pageobject.HomePage
import com.tle.webtests.pageobject.portal.MenuSection
import com.tle.webtests.test.AbstractCleanupAutoTest
import io.github.openequella.pages.hierarchy.{BrowseHierarchiesPage, HierarchyPanel}
import org.testng.Assert.{assertFalse, assertTrue}
import org.testng.annotations.Test
import testng.annotation.NewUIOnly

@TestInstitution("fiveo") class BrowseHierarchiesPageTest extends AbstractCleanupAutoTest {
  private val HIERARCHY_A_TOPI_NAME  = "A Topic"
  private val HIERARCHY_A_TOPIC_UUID = "e8c49738-7609-0079-e354-67b2e4e6b54c"
  private val CHILD_HIERARCHY_NAME   = "Child"
  private val A_TOPI_COUNT           = 2
  private val A_TOPIC_SHORT_DESC =
    "This is short descriptions"

  private def getHierarchyPanel: HierarchyPanel =
    new BrowseHierarchiesPage(context).load().hierarchyPanel

  @Test(description = "User should be able to access hierarchies page from the 'More' menu.")
  @NewUIOnly
  def accessFromMenu(): Unit = {
    new HomePage(context).load
    val menus      = new MenuSection(context).get
    val browsePage = menus.clickMenu("More...", new BrowseHierarchiesPage(context))
    assertTrue(browsePage.isLoaded)
  }

  @Test(description = "User should be able to see hierarchy details.")
  @NewUIOnly
  def seeDetails(): Unit = {
    val hierarchyPanel = getHierarchyPanel
    assertTrue(hierarchyPanel.hasHierarchy(HIERARCHY_A_TOPI_NAME))
    assertTrue(hierarchyPanel.isResultCountShowed(HIERARCHY_A_TOPI_NAME, A_TOPI_COUNT))
    assertTrue(hierarchyPanel.hasHierarchyShortDesc(A_TOPIC_SHORT_DESC))
  }

  @Test(description = "User should be able to expand hierarchy tree.")
  @NewUIOnly
  def expandHierarchy(): Unit = {
    val hierarchyPanel = getHierarchyPanel

    hierarchyPanel.expandHierarchy(HIERARCHY_A_TOPI_NAME)
    assertTrue(hierarchyPanel.hasHierarchy(CHILD_HIERARCHY_NAME))
  }

  @Test(description = "User should be able to navigate to hierarchy page.")
  @NewUIOnly
  def navigateToHierarchyPage(): Unit = {
    val hierarchyPanel = getHierarchyPanel

    val hierarchyPage =
      hierarchyPanel.clickHierarchy(HIERARCHY_A_TOPI_NAME, HIERARCHY_A_TOPIC_UUID)

    assertTrue(hierarchyPage.isLoaded)
  }

  @NewUIOnly
  @Test(description = "Should display 0 count if hierarchy has no result.")
  def noResults(): Unit = {
    val topic = "No Results"

    // Make sure matched number count is 0 in Browse page
    val browsePage = new BrowseHierarchiesPage(context).load
    assertTrue(browsePage.hierarchyPanel.isResultCountShowed(topic, 0))
  }

  @NewUIOnly
  @Test(description = "Do not display search result count if its set to hide search result.")
  def hiddenResults(): Unit = {
    val topic = "Results not shown"

    // Make sure matched count is not shown.
    val browsePage = new BrowseHierarchiesPage(context).load
    assertFalse(browsePage.hierarchyPanel.hasHierarchyResultCount(topic))
  }
}
