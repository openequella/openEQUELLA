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
import com.tle.webtests.pageobject.searching.SearchPage
import com.tle.webtests.pageobject.viewitem.SummaryPage
import com.tle.webtests.pageobject.wizard.ContributePage
import com.tle.webtests.test.AbstractSessionTest
import io.github.openequella.pages.dashboard.PortletType
import io.github.openequella.pages.dashboard.portlets.{FavouritesPortlet, PortletFactory}
import io.github.openequella.pages.favourites.FavouritesPage
import org.testng.Assert.{assertFalse, assertTrue}
import org.testng.annotations.{BeforeClass, BeforeMethod, Test}
import testng.annotation.NewUIOnly

@NewUIOnly
@TestInstitution("vanilla")
class FavouritesPortletTest extends AbstractPortletTest {
  var favPortletName: String = _
  var itemName: String       = _
  var searchName: String     = _

  var favouritesPortlet: FavouritesPortlet = _

  @BeforeClass
  def setupClass(): Unit = {
    favPortletName = context.getFullName("Favourites")
    itemName = context.getFullName("add me")
    searchName = context.getFullName("search")

    loginWithPortletAccount()
    // Create and favourite an item.
    createItem(itemName).addToFavourites().clickAdd()
    // Favourite a search.
    favouriteSearch(searchName)
    // Create portlet.
    createFavouritesPortlet(favPortletName)
  }

  @BeforeMethod
  def setupFavouritesPortletTest(): Unit = {
    favouritesPortlet = getFavouritesPortlet
  }

  @Test(description = "Should display the favourite resource")
  def showFavouriteResource(): Unit = {
    assertTrue(favouritesPortlet.hasResource(itemName))
  }

  @Test(description =
    "Should display the favourite resource page after clicking Show All under resource tab"
  )
  def showAllFavouriteResource(): Unit = {
    favouritesPortlet.clickShowAllButton()

    val favouritesPage = new FavouritesPage(context).get()
    favouritesPage.waitForSearchCompleted()
    assertTrue(favouritesPage.hasItem(itemName))
  }

  @Test(
    description = "Should not display the favourite resource after removing it",
    dependsOnMethods = Array("showFavouriteResource")
  )
  def notShowResourceAfterRemoving(): Unit = {
    // Remove the favourite item.
    val summaryPage = favouritesPortlet.clickResource(itemName)
    summaryPage.removeFavourite()

    loadDashboardPage()
    val favouritesPortletAfterRemoval = getFavouritesPortlet
    assertFalse(favouritesPortletAfterRemoval.hasResource(itemName))
  }

  @Test(description = "Should display the favourite search")
  def showFavouriteSearch(): Unit = {
    favouritesPortlet.clickSearchesTab()
    assertTrue(favouritesPortlet.hasSearch(searchName))
  }

  @Test(description =
    "Should display the favourite search page after clicking Show All under search tab"
  )
  def showAllFavouriteSearch(): Unit = {
    favouritesPortlet.clickSearchesTab()
    favouritesPortlet.clickShowAllButton()

    val favouritesPage = new FavouritesPage(context).get()
    favouritesPage.selectFavouritesSearchesType()
    favouritesPage.waitForSearchCompleted()
    assertTrue(favouritesPage.hasSearch(searchName))
  }

  @Test(
    description = "Should not display the favourite search after removing it",
    dependsOnMethods = Array("showFavouriteSearch")
  )
  def notShowSearchAfterRemoving(): Unit = {
    // Remove favourite search.
    val favouritesPage = new FavouritesPage(context).load()
    favouritesPage.selectFavouritesSearchesType()
    favouritesPage.removeFavourite(searchName)

    loadDashboardPage()
    val favouritesPortletAfterRemoval = getFavouritesPortlet
    assertFalse(favouritesPortletAfterRemoval.hasSearch(searchName))
  }

  private def favouriteSearch(searchName: String): Unit = {
    val search = new SearchPage(context).load()
    search.search(itemName)
    search.saveSearch(searchName)
  }

  private def createItem(itemName: String): SummaryPage = {
    val wizard =
      new ContributePage(context)
        .load()
        .openWizard(AbstractSessionTest.GENERIC_TESTING_COLLECTION)
    wizard.editbox(1, itemName)
    wizard.save().publish()
  }

  private def createFavouritesPortlet(name: String): Unit = {
    loadDashboardPage()
    dashboardPage.createPortlet(PortletType.Favourites, name)
  }

  private def getFavouritesPortlet: FavouritesPortlet =
    dashboardPage.getPortlet(PortletFactory.Favourites, favPortletName)
}
