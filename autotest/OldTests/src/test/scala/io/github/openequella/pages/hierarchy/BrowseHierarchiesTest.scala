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

import com.tle.webtests.framework.TestInstitution
import com.tle.webtests.pageobject.HomePage
import com.tle.webtests.pageobject.portal.MenuSection
import com.tle.webtests.test.AbstractCleanupAutoTest
import org.testng.Assert.assertTrue
import org.testng.annotations.Test
import testng.annotation.NewUIOnly

@TestInstitution("rest") class BrowseHierarchiesTest extends AbstractCleanupAutoTest {
  private val HIERARCHY_API_TEST_CLIENT_NAME = "HierarchyApiTestClient"
  private val PARENT_HIERARCHY_NAME          = "Parent Topics"
  private val CHILD_HIERARCHY_NAME           = "Child Topic"
  private val BROWSE_BOOKS_NAME              = "Browse books"
  private val BROWSE_BOOKS_COUNT             = 6
  private val HIERARCHY_SHORT_DESC =
    "a simple one level persistent hierarchy for make benefit glorious HierarchyApiTest"

  private def openBrowsePage(): BrowseHierarchiesPage =
    new BrowseHierarchiesPage(context).load()

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
    val browsePage = openBrowsePage()
    assertTrue(browsePage.hasHierarchy(HIERARCHY_API_TEST_CLIENT_NAME))
    assertTrue(browsePage.hasHierarchyMatchedCount(BROWSE_BOOKS_NAME, BROWSE_BOOKS_COUNT))
    assertTrue(browsePage.hasHierarchyShortDesc(HIERARCHY_SHORT_DESC))
  }

  @Test(description = "User should be able to expand hierarchy tree.")
  @NewUIOnly
  def expandHierarchy(): Unit = {
    val browsePage = openBrowsePage()

    browsePage.expandHierarchy(PARENT_HIERARCHY_NAME)
    assertTrue(browsePage.hasHierarchy(CHILD_HIERARCHY_NAME))
  }

  @Test(description = "User should be able to navigate to hierarchy page.")
  @NewUIOnly
  def navigateToHierarchyPage(): Unit = {
    val browsePage = openBrowsePage()

    val hierarchyPage = browsePage.clickHierarchy(HIERARCHY_API_TEST_CLIENT_NAME)
    hierarchyPage.waitForLoading()

    assertTrue(hierarchyPage.isLoaded)
  }
}
