package com.tle.webtests.test.myresources.favourites;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.portal.MenuSection;
import com.tle.webtests.pageobject.searching.FavouriteItemsPage;
import com.tle.webtests.pageobject.searching.FavouritesPage;
import com.tle.webtests.pageobject.searching.ItemListPage;
import com.tle.webtests.pageobject.searching.ItemSearchResult;
import com.tle.webtests.pageobject.searching.SearchPage;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.test.AbstractCleanupTest;
import org.testng.annotations.Test;
import testng.annotation.OldUIOnly;

@TestInstitution("vanilla")
public class FavouriteItemsTest extends AbstractCleanupTest {

  @Test
  @OldUIOnly
  public void fromItemSummary() {
    logon("AutoTest", "automated");
    String itemName = context.getFullName("testfromsummary");

    // Add Item
    createItem(itemName);

    // Add to favourites from summary page.
    assertAddFavouriteFromSummary(itemName);

    // Check that it appears on the Favourite Items page
    assertItemVisibleOnFavourites(itemName);

    // Remove it from favourites from summary page.
    assertRemoveFavouriteFromSummary(itemName);
  }

  @Test
  @OldUIOnly
  public void fromSearchResults() {
    logon("AutoTest", "automated");
    String itemName = context.getFullName("testfromsearchresult");

    // Add Item
    createItem(itemName);

    // Add it to favourites from search results.
    assetAddFavouriteFromSearchPage(itemName);

    // Check that it appears on the Favourite Items page
    assertItemVisibleOnFavourites(itemName);

    // Remove Favourite with link on search result
    assertRemoveFavouriteFromSearchPage(itemName);
  }

  @Test
  @OldUIOnly
  public void testFavouriteTagSearch() {
    logon("AutoTest", "automated");
    String name1 = "testtagsearch1";
    String itemName1 = context.getFullName(name1);
    String name2 = "testtagsearch2";
    String itemName2 = context.getFullName(name2);
    String tag1 = "tagnumberone";
    String tag2 = "tagnumbertwo,three;four five";

    // Add Item 1 and Item 2
    createItem(itemName1);
    createItem(itemName2);

    // Search for item1 and add it to favourites with tag1
    assetAddFavouriteFromSearchPage(itemName1, tag1);

    // Search for item2 and add it to favourites with tag2
    assetAddFavouriteFromSearchPage(itemName2, tag2);

    // Goto favourites page and assert both items are present before filtering
    FavouriteItemsPage favs = new FavouritesPage(context).load().items();
    assertTrue(favs.results().doesResultExist(itemName1));
    assertTrue(favs.results().doesResultExist(itemName2));

    // Search favs with tag/keyword filter and verify which items are returned
    ItemListPage favresults = favs.search(tag1);
    assertTrue(favresults.doesResultExist(itemName1, 1));
    assertFalse(favresults.doesResultExist(itemName2));

    favresults = favs.search("tagnumbertwo");
    assertTrue(favresults.doesResultExist(itemName2, 1));
    assertFalse(favresults.doesResultExist(itemName1));

    favresults = favs.search("three");
    assertTrue(favresults.doesResultExist(itemName2, 1));
    assertFalse(favresults.doesResultExist(itemName1));

    favresults = favs.search("four");
    assertTrue(favresults.doesResultExist(itemName2, 1));
    assertFalse(favresults.doesResultExist(itemName1));

    favresults = favs.search("five");
    assertTrue(favresults.doesResultExist(itemName2, 1));
    assertFalse(favresults.doesResultExist(itemName1));

    favresults = favs.search("three four five");
    assertTrue(favresults.doesResultExist(itemName2, 1));
    assertFalse(favresults.doesResultExist(itemName1));
  }

  @Test
  @OldUIOnly
  public void testVersionFavourites() {
    logon("AutoTest", "automated");
    String itemName = context.getFullName("version");
    String thisVersionTag = "thisversion";
    String latestVersionTag = "latestversion";

    // try / catch
    createItem(itemName);

    // Favourite v1 as "This version"
    addFavouriteForVersion(itemName, thisVersionTag, /* latest= */ false);

    // Create v2
    createNewVersion(itemName);

    // Favourite v2 as "Latest version"
    addFavouriteForVersion(itemName, latestVersionTag, /* latest= */ true);

    // Verify 'This Version' points to v1 and 'Latest Version' points to v2
    assertFavouriteVersion(thisVersionTag, itemName, 1);
    assertFavouriteVersion(latestVersionTag, itemName, 2);

    // Create v3
    createNewVersion(itemName);

    // Verify "Latest version" now points to v3
    assertFavouriteVersion(latestVersionTag, itemName, 3);
  }

  @Test
  @OldUIOnly
  public void testNoResults() {
    logon("TLE_ADMINISTRATOR", testConfig.getAdminPassword());

    assertNoFavouriteResults();
  }

  @Test
  @OldUIOnly
  public void testAutoLoggedIn() {
    logout().autoLogin(); // Ensure logged out

    // Assert there is no favourite menu
    MenuSection menu = new MenuSection(context).get();
    assertFalse(menu.hasMenuOption("Favourites"));

    // Assert there is no add search to favs button
    SearchPage searchPage = menu.clickMenu("Search", new SearchPage(context));
    assertFalse(searchPage.hasFavouriteSearchOption());

    // Assert there is no add to favs button on item summary
    searchPage.results().getResult(1).clickTitle();
    SummaryPage summaryPage = new SummaryPage(context);
    assertFalse(summaryPage.hasFavouriteOption());
  }

  /** Creates and publishes a basic test item with the provided name. */
  private void createItem(String itemName) {
    WizardPageTab wizard =
        new ContributePage(context).load().openWizard(GENERIC_TESTING_COLLECTION);
    wizard.editbox(1, itemName);
    wizard.save().publish();
  }

  /** Adds an item to favourites from the summary page and asserts success. */
  private void assertAddFavouriteFromSummary(String itemName) {
    SummaryPage itemSummary =
        new SearchPage(context)
            .load()
            .exactQuery(itemName)
            .getResultForTitle(itemName, 1)
            .viewSummary();
    itemSummary.addToFavourites().clickAdd();
    assertTrue(itemSummary.isFavouriteItem());
  }

  /** Adds an item to favourites from the search page and asserts success. */
  private void assetAddFavouriteFromSearchPage(String itemName, String... tags) {
    SearchPage searchPage = new SearchPage(context).load();
    ItemListPage results = searchPage.exactQuery(itemName);
    ItemSearchResult resultForTitle = results.getResultForTitle(itemName, 1);
    if (tags != null && tags.length > 0) {
      resultForTitle.addToFavourites().setTags(tags[0]).clickAdd();
    } else {
      resultForTitle.addToFavourites().clickAdd();
    }
    assertTrue(resultForTitle.isFavouriteItem());
  }

  /** Removes an item from favourites from the summary page and asserts success. */
  private void assertRemoveFavouriteFromSummary(String itemName) {
    SummaryPage itemSummary =
        new SearchPage(context)
            .load()
            .exactQuery(itemName)
            .getResultForTitle(itemName, 1)
            .viewSummary();
    itemSummary.removeFavourite();
    itemSummary.get();
    assertFalse(itemSummary.isFavouriteItem());
  }

  /** Removes an item from favourites from the search page and asserts success. */
  private void assertRemoveFavouriteFromSearchPage(String itemName) {
    SearchPage searchPage = new SearchPage(context).load();
    ItemListPage results = searchPage.exactQuery(itemName);
    ItemSearchResult resultForTitle = results.getResultForTitle(itemName, 1);
    resultForTitle.removeFavourite();

    results = new SearchPage(context).load().exactQuery(itemName);
    resultForTitle = results.getResultForTitle(itemName, 1);
    assertFalse(resultForTitle.isFavouriteItem());
  }

  /** Asserts an item is visible on the favourites page, optionally filtering by tag. */
  private void assertItemVisibleOnFavourites(String itemName, String... tags) {
    FavouriteItemsPage favs = new FavouritesPage(context).load().items();
    if (tags != null && tags.length > 0) {
      ItemListPage favresults = favs.search(tags[0]);
      assertTrue(favresults.doesResultExist(itemName, 1));
    } else {
      assertTrue(favs.results().doesResultExist(itemName));
    }
  }

  /** Asserts that the favourites page shows no results. */
  private void assertNoFavouriteResults() {
    FavouriteItemsPage favs = new FavouritesPage(context).load().items();
    assertFalse(favs.hasResults());
  }

  /** Adds a version-specific favourite from the search page. */
  private void addFavouriteForVersion(String itemName, String tag, boolean latestVersion) {
    SearchPage searchPage = new SearchPage(context).load();
    ItemListPage results = searchPage.exactQuery(itemName);
    assertTrue(results.doesResultExist(itemName, 1));

    ItemSearchResult resultForTitle = results.getResultForTitle(itemName, 1);
    resultForTitle.addToFavourites().setTags(tag).setLatestVersion(latestVersion).clickAdd();
  }

  /** Creates a new version of an item from its summary page. */
  private void createNewVersion(String itemName) {
    WizardPageTab wizard = SearchPage.searchAndView(context, itemName).adminTab().newVersion();
    wizard.save().publish();
  }

  /** Asserts a favourite points to the correct item version. */
  private void assertFavouriteVersion(String tag, String itemName, int expectedVersion) {
    FavouriteItemsPage favs = new FavouritesPage(context).load().items();
    ItemListPage favresults = favs.search(tag);
    SummaryPage summary = favresults.getResult(1).viewSummary();
    assertEquals(expectedVersion, summary.getItemId().getVersion());
  }
}
