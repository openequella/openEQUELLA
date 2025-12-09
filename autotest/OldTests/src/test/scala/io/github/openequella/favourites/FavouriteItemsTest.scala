package io.github.openequella.favourites

import com.tle.webtests.framework.TestInstitution
import com.tle.webtests.pageobject.portal.MenuSection
import com.tle.webtests.pageobject.wizard.ContributePage
import com.tle.webtests.test.AbstractCleanupAutoTest
import io.github.openequella.pages.favourites.FavouritesPage
import io.github.openequella.pages.search.NewSearchPage
import org.testng.Assert.{assertEquals, assertFalse, assertTrue}
import org.testng.annotations.Test
import testng.annotation.NewUIOnly

@TestInstitution("fiveo")
@NewUIOnly
class FavouriteItemsTest extends AbstractCleanupAutoTest {

  @Test(description = "Verify adding and removing an item from favourites on the item summary page")
  def fromItemSummary(): Unit = {
    val itemName = context.getFullName("summary")

    createTestItem(itemName)
    addFavouriteFromSummaryPage(itemName)
    assertItemInFavouritesPage(itemName)
    removeFavouriteFromSummaryPage(itemName)
  }

  @Test(description =
    "Verify adding and removing an item from favourites on the search results page"
  )
  def fromSearchResults(): Unit = {
    val itemName = context.getFullName("search")

    createTestItem(itemName)
    addFavouriteFromSearchPage(itemName)
    assertItemInFavouritesPage(itemName)
    removeFavouriteFromSearchPage(itemName)
  }

  @Test(description = "Verify favourites options are not shown when not logged in / auto logged in")
  def testAutoLoggedIn(): Unit = {
    logout().autoLogin()

    // Verify `Favourites` menu option is not shown
    val menu = new MenuSection(context).get
    assertFalse(
      menu.hasMenuOption("Favourites"),
      "Favourites menu should not be present for auto-logged in users"
    )

    // Open Item summary page for the first item and verify that add/remove favourite button is not shown
    val searchPage = new NewSearchPage(context).load()
    searchPage.waitForInitialSearchResult()
    val firstItem   = searchPage.getItemNameByIndex(0)
    val summaryPage = searchPage.selectItem(firstItem)
    assertFalse(
      summaryPage.hasFavouriteOption,
      "Favourite option should not be available on summary page"
    )
  }

  @Test(description = "Verify version specific favourites (This Version vs Latest Version)")
  def testVersionFavourites(): Unit = {
    val itemName         = context.getFullName("version")
    val tagThisVersion   = "thisversion"
    val tagLatestVersion = "latestversion"

    // Create Item (Version 1)
    createTestItem(itemName)

    // Favourite v1 as "This version"
    val searchPage = new NewSearchPage(context).load()

    searchPage.changeQuery(itemName)
    searchPage.waitForSearchCompleted(1)
    searchPage.addItemToFavourites(itemName, Array(tagThisVersion), false)

    searchPage.newSearch();

    // Create Version 2
    createItemNewVersion(itemName)

    searchPage.load()

    // Favourite v2 as "Latest version"
    searchPage.changeQuery(itemName)
    searchPage.waitForSearchCompleted(1)
    searchPage.addItemToFavourites(itemName, Array(tagLatestVersion), true)

    // Verify 'This Version' points to v1 and 'Latest Version' points to v2
    assertFavouriteVersion(tagThisVersion, itemName, 1)
    assertFavouriteVersion(tagLatestVersion, itemName, 2)

    // Create Version 3
    createItemNewVersion(itemName)

    // Verify 'Latest Version' now points to v3
    assertFavouriteVersion(tagLatestVersion, itemName, 3)
  }

  /** Loads the Favourites page, optionally configuring it to show all versions.
    *
    * @param allVersions
    *   when true, expands the refine panel and selects 'All' status values.
    */
  private def loadFavouritesPage(allVersions: Boolean = false): FavouritesPage = {
    val favouritesPage = new FavouritesPage(context).load()
    if (allVersions) {
      favouritesPage.expandRefineControlPanel()
      favouritesPage.selectStatus(true)
      favouritesPage.waitForSearchCompleted()
    }
    favouritesPage
  }

  /** Creates and publishes a basic test item with the provided name.
    *
    * @param itemName
    *   name to use for the contributed item.
    */
  private def createTestItem(itemName: String): Unit = {
    val wizard = new ContributePage(context).load().openWizard("Basic Items")
    wizard.editbox(1, itemName)
    wizard.save().publish()
  }

  /** Navigates to the item summary and clicks the add-to-favourites button.
    *
    * @param itemName
    *   name of the item to locate in search before opening summary.
    */
  private def addFavouriteFromSummaryPage(itemName: String): Unit = {
    val searchPage = new NewSearchPage(context).load()
    searchPage.changeQuery(itemName)
    searchPage.waitForSearchCompleted(1)

    val summaryPage = searchPage.selectItem(itemName)
    summaryPage.addToFavourites().clickAdd()
  }

  /** Navigates to the item summary and removes the item from favourites, asserting the summary no
    * longer reports the item as favourited.
    *
    * @param itemName
    *   name of the item whose favourite flag should be removed.
    */
  private def removeFavouriteFromSummaryPage(itemName: String): Unit = {
    val searchPage = new NewSearchPage(context).load()
    searchPage.changeQuery(itemName)
    searchPage.waitForSearchCompleted(1)

    val summaryPage = searchPage.selectItem(itemName)
    summaryPage.removeFavourite()
    assertFalse(summaryPage.isFavouriteItem)
  }

  /** Adds the given item to favourites from the search results list.
    *
    * @param itemName
    *   name of the item to favourite in the current search results.
    */
  private def addFavouriteFromSearchPage(itemName: String): Unit = {
    val searchPage = new NewSearchPage(context).load()
    searchPage.changeQuery(itemName)
    searchPage.waitForSearchCompleted(1)
    searchPage.addItemToFavourites(itemName)
  }

  /** Removes the given item from favourites in the search results and asserts it is no longer
    * marked as a favourite in the list.
    *
    * @param itemName
    *   name of the item to unfavourite from the search results.
    */
  private def removeFavouriteFromSearchPage(itemName: String): Unit = {
    val searchPage = new NewSearchPage(context).load()
    searchPage.changeQuery(itemName)
    searchPage.waitForSearchCompleted(1)
    searchPage.removeFavouriteFromSearchResult(itemName)
    assertFalse(searchPage.isItemFavourited(itemName))
  }

  /** Loads the Favourites page and asserts that the specified item appears in the results.
    *
    * @param itemName
    *   name of the item expected to be present on the Favourites page.
    */
  private def assertItemInFavouritesPage(itemName: String): Unit = {
    val favouritesPage = loadFavouritesPage()
    favouritesPage.waitForSearchCompleted()
    assertTrue(favouritesPage.hasItem(itemName))
  }

  /** Creates a new version of the specified item via the summary page.
    *
    * @param itemName
    *   name of the item for which a new version should be created.
    */
  private def createItemNewVersion(itemName: String): Unit = {
    val searchPage = new NewSearchPage(context).load()
    searchPage.changeQuery(itemName)
    searchPage.waitForSearchCompleted(1)

    val summaryPage = searchPage.selectItem(itemName)
    val wizard      = summaryPage.adminTab().newVersion()
    wizard.save().publish()
  }

  /** Verifies that a favourite tagged with the given name resolves to the expected item version on
    * the Favourites page.
    *
    * @param tagName
    *   tag applied when the favourite entry was created.
    * @param itemName
    *   name of the favourited item.
    * @param expectedVersion
    *   item version expected when opening the favourite.
    */
  private def assertFavouriteVersion(
      tagName: String,
      itemName: String,
      expectedVersion: Int
  ): Unit = {
    val favouritesPage = loadFavouritesPage(allVersions = true)

    favouritesPage.changeQuery(tagName)
    favouritesPage.waitForSearchCompleted(1)
    assertTrue(favouritesPage.hasItem(itemName))

    val summaryPage = favouritesPage.selectItem(itemName)
    assertEquals(summaryPage.getItemId.getVersion, expectedVersion)
  }
}
