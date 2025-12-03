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

package io.github.openequella.favourites

import com.tle.webtests.framework.TestInstitution
import com.tle.webtests.pageobject.HomePage
import com.tle.webtests.pageobject.portal.MenuSection
import com.tle.webtests.pageobject.wizard.ContributePage
import com.tle.webtests.test.AbstractCleanupAutoTest
import io.github.openequella.pages.favourites.FavouritesPage
import io.github.openequella.pages.search.NewSearchPage
import org.testng.Assert.{assertFalse, assertTrue}
import org.testng.annotations.Test
import testng.annotation.NewUIOnly

@TestInstitution("fiveo")
class FavouritesPageTest extends AbstractCleanupAutoTest {
  private val CUSTOM_TAG = "customTag"

  private def setupFavouriteItem(itemName: String): Unit = {
    val tags = Array(CUSTOM_TAG)
    // Create an item to favourite.
    val wizard = new ContributePage(context).load.openWizard("Basic Items")
    wizard.editbox(1, itemName)
    wizard.save.publish()

    // Add the item to favourites with tags.
    val searchPage = new NewSearchPage(context).load()
    searchPage.changeQuery(itemName)
    searchPage.waitForSearchCompleted(1)
    searchPage.addItemToFavourites(itemName, tags)
  }

  private def setupFavouriteSearch(searchName: String): Unit = {
    val searchPage = new NewSearchPage(context).load()
    searchPage.changeQuery("test")
    searchPage.waitForSearchCompleted()
    searchPage.addToFavouriteSearch(searchName)
  }

  @NewUIOnly
  @Test(description = "User should be able to access favourites page from the menu.")
  def accessFromMenu(): Unit = {
    new HomePage(context).load
    val menus = new MenuSection(context).get
    val favouritesPage =
      menus.clickMenu("Favourites", new FavouritesPage(context))
    assertTrue(favouritesPage.isLoaded)
  }

  @NewUIOnly
  @Test(description = "Verify empty state for favourites")
  def noResults(): Unit = {
    logout()
    logon("TLE_ADMINISTRATOR", testConfig.getAdminPassword)

    val favouritesPage = new FavouritesPage(context).load()
    assertTrue(favouritesPage.isSearchResultListEmpty)

    favouritesPage.selectFavouritesSearchesType();
    assertTrue(favouritesPage.isSearchResultListEmpty)
  }

  @NewUIOnly
  @Test(description = "Verify switching between favourite types")
  def switchFavouriteType(): Unit = {
    val favouritesPage = new FavouritesPage(context).load()
    favouritesPage.selectFavouritesSearchesType()
    assertTrue(favouritesPage.isToggleButtonSelected("Searches"), "Searches should be selected")

    favouritesPage.selectFavouritesResourcesType()
    assertTrue(favouritesPage.isToggleButtonSelected("Resources"), "Resources should be selected")
  }

  @NewUIOnly
  @Test(description = "Verify user can search for favourite resources by tags")
  def searchByTags(): Unit = {
    val item = context.getFullName("search")
    setupFavouriteItem(item)

    // Navigate to the Favourites page.
    val favouritesPage = new FavouritesPage(context).load()
    favouritesPage.selectFavouritesResourcesType()

    // Check the item is present.
    assertTrue(favouritesPage.hasItem(item))

    // Search by tag and verify.
    favouritesPage.changeQuery(CUSTOM_TAG)
    favouritesPage.waitForSearchCompleted(1)
    assertTrue(favouritesPage.hasItem(item))
  }

  @NewUIOnly
  @Test(description = "Verify user can remove a favourite resource")
  def removeFavouriteItem(): Unit = {
    val item = context.getFullName("item")
    setupFavouriteItem(item)

    // Navigate to the Favourites page.
    val favouritesPage = new FavouritesPage(context).load()
    favouritesPage.selectFavouritesResourcesType()

    // Check the item is present.
    assertTrue(favouritesPage.hasItem(item))

    // Remove the item from favourites.
    favouritesPage.removeFromFavourites(item)
    favouritesPage.waitForSearchCompleted(0)
    assertFalse(favouritesPage.hasItem(item))
  }

  @NewUIOnly
  @Test(description = "Verify user can remove a favourite search")
  def removeFavouriteSearch(): Unit = {
    val search = context.getFullName("favSearch")
    setupFavouriteSearch(search);

    val favouritesPage = new FavouritesPage(context).load()
    favouritesPage.selectFavouritesSearchesType()

    // Check the item is present.
    assertTrue(favouritesPage.hasItem(search))

    // Remove the search from favourites.
    favouritesPage.removeFromFavourites(search)
    assertFalse(favouritesPage.hasItem(search))
  }
}
