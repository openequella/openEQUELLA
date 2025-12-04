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
import com.tle.webtests.pageobject.wizard.ContributePage
import com.tle.webtests.test.AbstractCleanupAutoTest
import io.github.openequella.pages.favourites.FavouritesPage
import io.github.openequella.pages.hierarchy.{BrowseHierarchiesPage, HierarchyPage}
import io.github.openequella.pages.search.NewSearchPage
import org.testng.Assert.{assertEquals, assertFalse, assertTrue}
import org.testng.annotations.Test
import testng.annotation.NewUIOnly

/** New UI tests for hierarchy page.
  */
@TestInstitution("fiveo") class HierarchyPageTest extends AbstractCleanupAutoTest {
  private val A_TOPIC_NAME            = "A Topic"
  private val A_TOPIC_UUID            = "e8c49738-7609-0079-e354-67b2e4e6b54c"
  private val CHILD_TOPIC_NAME        = "Child"
  private val CHILD_TOPIC_UUID        = "e3988e94-7e76-ee78-4b46-086ba1dda897"
  private val CHILD_HIDDEN_TOPIC_NAME = "Some Children Hidden"
  private val CHILD_HIDDEN_TOPIC_UUID = "33ecf7c7-24a6-f01d-d83e-38e9782c3803"

  private val BASIC_ITEM  = "SearchFilters - Basic Item"
  private val RANDOM_ITEM = "SearchSettings - Random Item"

  private def openHierarchyPage(hierarchyName: String, compoundUuid: String): HierarchyPage =
    new HierarchyPage(context, hierarchyName, compoundUuid).load()

  // Add key resource to hierarchy to prepare for the test.
  private def prepareKeyResource(hierarchyPage: HierarchyPage, itemName: String): Unit = {
    hierarchyPage.addKeyResourceFromResultList(itemName)
    assertTrue(hierarchyPage.hasKeyResource(itemName))
  }

  @NewUIOnly
  @Test(description = "User should be able to access hierarchy page from the menu.")
  def accessFromMenu(): Unit = {
    new HomePage(context).load
    val menus = new MenuSection(context).get
    val hierarchyPage =
      menus.clickMenu(A_TOPIC_NAME, new HierarchyPage(context, A_TOPIC_NAME, A_TOPIC_UUID))
    assertTrue(hierarchyPage.isLoaded)
  }

  @NewUIOnly
  @Test(description = "Child topic should inherit results from parent topic.")
  def childInheritance(): Unit = {
    val hierarchyPage = openHierarchyPage(A_TOPIC_NAME, A_TOPIC_UUID)
    hierarchyPage.waitForInitialSearchResult()

    assertTrue(hierarchyPage.hasItem(BASIC_ITEM))
    assertTrue(hierarchyPage.hasItem(RANDOM_ITEM))

    val childHierarchyPage = openHierarchyPage(CHILD_TOPIC_NAME, CHILD_TOPIC_UUID)
    childHierarchyPage.waitForInitialSearchResult()
    // This child topic inherits the two Items, but due to its own query configuration, it shows only one Item.
    assertTrue(childHierarchyPage.hasItem(RANDOM_ITEM))
    assertFalse(childHierarchyPage.hasItem(BASIC_ITEM))
  }

  @NewUIOnly
  @Test(
    description = "User should be able to add key resource by clicking the pin icon in item list."
  )
  def addKeyResourceFromResultList(): Unit = {
    val hierarchyPage = openHierarchyPage(A_TOPIC_NAME, A_TOPIC_UUID)

    assertFalse(hierarchyPage.isItemPinIconHighlighted(BASIC_ITEM))

    hierarchyPage.addKeyResourceFromResultList(BASIC_ITEM)

    assertTrue(hierarchyPage.hasKeyResource(BASIC_ITEM))
    assertTrue(hierarchyPage.isItemPinIconHighlighted(BASIC_ITEM))

    // remove key resource
    hierarchyPage.removeKeyResourceFromSearchResult(BASIC_ITEM)
  }

  @NewUIOnly
  @Test(
    description =
      "User should be able to remove key resource by clicking the pin icon in result list."
  )
  def removeKeyResourceFromResultList(): Unit = {
    val hierarchyPage = openHierarchyPage(A_TOPIC_NAME, A_TOPIC_UUID)
    prepareKeyResource(hierarchyPage, BASIC_ITEM)

    hierarchyPage.removeKeyResourceFromSearchResult(BASIC_ITEM)

    assertFalse(hierarchyPage.hasKeyResource(BASIC_ITEM))
    assertFalse(hierarchyPage.isItemPinIconHighlighted(BASIC_ITEM))
  }

  @NewUIOnly
  @Test(
    description =
      "User should be able to remove key resource by clicking the pin icon in key resource panel."
  )
  def removeKeyResourceFromKeyResourcePanel(): Unit = {
    val hierarchyPage = openHierarchyPage(A_TOPIC_NAME, A_TOPIC_UUID)
    prepareKeyResource(hierarchyPage, RANDOM_ITEM)

    hierarchyPage.removeKeyResourceFromKeyResourcePanel(RANDOM_ITEM)

    assertFalse(hierarchyPage.hasKeyResource(RANDOM_ITEM))
    assertFalse(hierarchyPage.isItemPinIconHighlighted(RANDOM_ITEM))
  }

  @Test(description = "Adds item as a key resource from search page modify key resource dialog")
  @NewUIOnly
  def addKeyResourceFromSearchPage(): Unit = {
    val hierarchyName = "Power Search"
    val hierarchyUuid = "d62bbe4e-84d9-06f2-62e1-31ddc28a1ee6"
    val itemName      = "DRM API test"

    val searchPage = new NewSearchPage(context).load
    searchPage.waitForInitialSearchResult()
    searchPage.addToKeyResource(itemName, hierarchyName)

    val hierarchyPage = openHierarchyPage(hierarchyName, hierarchyUuid)
    assertTrue(hierarchyPage.hasKeyResource(itemName))

    // remove key resource
    hierarchyPage.removeKeyResourceFromKeyResourcePanel(itemName)
  }

  @NewUIOnly
  @Test(description = "Should display all key resources.")
  def lotsOfKeyResources(): Unit = {
    val itemName              = context.getFullName("Lots of Key Resources")
    val originalResourceCount = openHierarchyPage(A_TOPIC_NAME, A_TOPIC_UUID).keyResourceCount

    // Add 11 key resources. These key resources will be removed due to extends from `AbstractCleanupAutoTest`.
    (0 until 11).foreach { i =>
      {
        val wiz = new ContributePage(context).load.openWizard("SOAP and Harvesting")
        wiz.editbox(1, s"$itemName $i")
        val item = wiz.save.publish.adminTab
        item.modifyKeyResource.addToHierarchy(A_TOPIC_NAME)
      }
    }

    val hierarchyPage = openHierarchyPage(A_TOPIC_NAME, A_TOPIC_UUID)

    // Make sure all key resource is present.
    assertEquals(hierarchyPage.keyResourceCount, 11 + originalResourceCount)
  }

  @NewUIOnly
  @Test(description = "Should be able to save a hierarchy search to favourite.")
  def saveAsFavourite(): Unit = {
    val hierarchyPage = openHierarchyPage(A_TOPIC_NAME, A_TOPIC_UUID)
    val searchName    = s"${context.getFullName(A_TOPIC_NAME)} saved"

    hierarchyPage.addToFavouriteSearch(searchName)

    // Check the saved search on favourites page
    val favouritesPage = new FavouritesPage(context).load
    favouritesPage.selectFavouritesSearchesType()
    favouritesPage.hasSearch(searchName)
    favouritesPage.selectSearch(searchName)

    // Make sure current hierarchy is A_TOPIC hierarchy and is loaded.
    val currentHierarchyPage = new HierarchyPage(context, A_TOPIC_NAME, A_TOPIC_UUID).get
    assertTrue(currentHierarchyPage.isLoaded)

    // Remove the saved search
    val newFavouritesPage = new FavouritesPage(context).load
    newFavouritesPage.selectFavouritesSearchesType()
    newFavouritesPage.removeFavourite(searchName)
  }

  @NewUIOnly
  @Test(description = "Should not display item if there is no result.")
  def noResults(): Unit = {
    val topic = "No Results"
    val uuid  = "cd42ec56-d893-a81d-ba59-00742414ff3d"

    // Make sure result is 0.
    val hierarchyPage = openHierarchyPage(topic, uuid)
    assertEquals(hierarchyPage.getResultCount, 0)
  }

  @NewUIOnly
  @Test(description = "Should display the section name above subtopic tree.")
  def subtopicSectionName(): Unit = {
    val hierarchyPage = openHierarchyPage(CHILD_HIDDEN_TOPIC_NAME, CHILD_HIDDEN_TOPIC_UUID)

    assertEquals(hierarchyPage.getSubtopicSectionName, "A name")
  }

  @NewUIOnly
  @Test(description = "Do not display search result if its set to hide search result")
  def hiddenResults(): Unit = {
    val topic = "Results not shown"
    val uuid  = "36b3f358-f229-0558-bfaf-59db6294978b"

    // Make sure result list is not shown.
    val hierarchyPage = openHierarchyPage(topic, uuid)
    assertFalse(hierarchyPage.hasSearchResultList)
  }

  @NewUIOnly
  @Test(description = "Should be able to hide child topics which do not have any search result.")
  def hideNoResultChildren(): Unit = {
    // if a hierarchy is configured to hide sub topics that have no results, all of these sub topics should not be shown in the hierarchy tree'
    val hierarchyPage = openHierarchyPage(CHILD_HIDDEN_TOPIC_NAME, CHILD_HIDDEN_TOPIC_UUID)
    assertFalse(hierarchyPage.hierarchyPanel.hasHierarchy("Hidden"))

    // Create an item so let the `hidden` hierarchy have one result.
    val newItemTitle = context.getFullName("SuperSecretWord")
    val wiz          = new ContributePage(context).load.openWizard("SOAP and Harvesting")
    wiz.editbox(1, newItemTitle)
    wiz.save.publish

    // Now the `hidden` topic should be displayed in hierarchy tree.
    val newHierarchyPage = openHierarchyPage(CHILD_HIDDEN_TOPIC_NAME, CHILD_HIDDEN_TOPIC_UUID)
    assertTrue(newHierarchyPage.hierarchyPanel.hasHierarchy("Hidden"))

    // Edit the new item to remove it from the `hidden` topic.
    val editPage = newHierarchyPage.selectItem(newItemTitle).adminTab.edit
    editPage.editbox(1, context.getFullName("NoMore"))
    editPage.saveNoConfirm
  }

  @NewUIOnly
  @Test(
    description = "navigate to the Browse page by clicking the Browse link from the breadcrumb."
  )
  def browseBreadcrumbTest(): Unit = {
    val hierarchyPage = openHierarchyPage(CHILD_TOPIC_NAME, CHILD_TOPIC_UUID)

    val browseHierarchiesPage =
      hierarchyPage.clickBreadcrumb("Browse", new BrowseHierarchiesPage(context))
    assertTrue(browseHierarchiesPage.isLoaded)
  }

  @NewUIOnly
  @Test(description = "Displays breadcrumb and navigate to the parent hierarchy page")
  def breadcrumbTest(): Unit = {
    val hierarchyPage = openHierarchyPage(CHILD_TOPIC_NAME, CHILD_TOPIC_UUID)
    assertEquals(hierarchyPage.breadcrumbCount, 3)

    val newHierarchyPage = hierarchyPage.clickBreadcrumb(
      A_TOPIC_NAME,
      new HierarchyPage(context, A_TOPIC_NAME, A_TOPIC_UUID)
    )
    assertTrue(newHierarchyPage.isLoaded)
    assertEquals(hierarchyPage.breadcrumbCount, 2)
  }
}
