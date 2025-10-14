package com.tle.webtests.test.myresources.favourites;

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

/*
 * A lot of this test could be refactored into methods
 */
@TestInstitution("vanilla")
public class FavouriteItemsTest extends AbstractCleanupTest {

  // TODO: OEQ-2610 re-enable it in new UI or write new test.
  @Test
  @OldUIOnly
  public void addRemoveFavouriteFromItemSummary() {
    logon("AutoTest", "automated");
    String name = "testfromsummary";

    // Add Item
    WizardPageTab wizard =
        new ContributePage(context).load().openWizard(GENERIC_TESTING_COLLECTION);
    String itemName = context.getFullName(name);
    wizard.editbox(1, itemName);
    wizard.save().publish();

    // Find the item and view it
    SearchPage searchPage = new SearchPage(context).load();
    ItemListPage results = searchPage.exactQuery(itemName);
    assertTrue(results.doesResultExist(itemName, 1));

    // Add it to favourites (Check that button changes)
    SummaryPage itemSummary = results.getResultForTitle(itemName, 1).viewSummary();
    itemSummary.addToFavourites().clickAdd();
    assertTrue(itemSummary.isFavouriteItem());

    // Check that it appears on the Favourite Items page
    FavouriteItemsPage favs = new FavouritesPage(context).load().items();
    assertTrue(favs.results().isResultsAvailable());
    assertTrue(favs.results().doesResultExist(itemName, 1));

    // Find the item and view it
    searchPage = new SearchPage(context).load();
    results = searchPage.exactQuery(itemName);
    assertTrue(results.doesResultExist(itemName, 1));

    // Remove it from favourites (Check that button changes)
    itemSummary = results.getResultForTitle(itemName, 1).viewSummary();
    itemSummary.removeFavourite();
    itemSummary = itemSummary.get();
    assertFalse(itemSummary.isFavouriteItem());
  }

  // TODO: OEQ-2610 re-enable it in new UI or write new test.
  @Test
  @OldUIOnly
  public void addRemoveFavouriteFromSearchResults() {
    logon("AutoTest", "automated");
    String name = "testfromsearchresult";

    // Add Item
    WizardPageTab wizard =
        new ContributePage(context).load().openWizard(GENERIC_TESTING_COLLECTION);
    String itemName = context.getFullName(name);
    wizard.editbox(1, itemName);
    wizard.save().publish();

    // Find the item
    SearchPage searchPage = new SearchPage(context).load();
    ItemListPage results = searchPage.exactQuery(itemName);
    assertTrue(results.doesResultExist(itemName, 1));

    // Add it to favourites (Check that link changes)
    ItemSearchResult resultForTitle = results.getResultForTitle(itemName, 1);
    resultForTitle.addToFavourites().clickAdd();

    searchPage = new SearchPage(context).load();
    results = searchPage.exactQuery(itemName);
    resultForTitle = results.getResultForTitle(itemName, 1);
    assertTrue(resultForTitle.isFavouriteItem());

    // Check that it appears on the Favourite Items page
    FavouriteItemsPage favs = new FavouritesPage(context).load().items();
    assertTrue(favs.results().doesResultExist(itemName, 1));

    // Remove Favourite with link on search result
    searchPage = new SearchPage(context).load();
    results = searchPage.exactQuery(itemName);
    resultForTitle = results.getResultForTitle(itemName, 1);
    resultForTitle.removeFavourite();

    // Check if it has been removed
    searchPage = new SearchPage(context).load();
    results = searchPage.exactQuery(itemName);
    resultForTitle = results.getResultForTitle(itemName, 1);
    assertFalse(resultForTitle.isFavouriteItem());
  }

  // TODO: OEQ-2610 re-enable it in new UI or write new test.
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
    WizardPageTab wizard =
        new ContributePage(context).load().openWizard(GENERIC_TESTING_COLLECTION);
    wizard.editbox(1, itemName1);
    wizard.save().publish();
    wizard = new ContributePage(context).load().openWizard(GENERIC_TESTING_COLLECTION);
    wizard.editbox(1, itemName2);
    wizard.save().publish();

    // Search for item and add it to favourites
    SearchPage searchPage = new SearchPage(context).load();
    ItemListPage results = searchPage.exactQuery(itemName1);
    assertTrue(results.doesResultExist(itemName1, 1));
    ItemSearchResult resultForTitle = results.getResultForTitle(itemName1, 1);
    resultForTitle.addToFavourites().setTags(tag1).clickAdd();

    // Search for item and add it to favourites
    searchPage = new SearchPage(context).load();
    results = searchPage.exactQuery(itemName2);
    assertTrue(results.doesResultExist(itemName2, 1));
    resultForTitle = results.getResultForTitle(itemName2, 1);
    resultForTitle.addToFavourites().setTags(tag2).clickAdd();

    // Goto favourites page and search for tags using keyword filter
    FavouriteItemsPage favs = new FavouritesPage(context).load().items();
    assertTrue(favs.results().doesResultExist(itemName1));
    assertTrue(favs.results().doesResultExist(itemName2));

    // Search favs with tag/keyword filter
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

    // Remove favourites
    searchPage = new SearchPage(context).load();
    results = searchPage.exactQuery(itemName1);
    assertTrue(results.doesResultExist(itemName1, 1));
    results.getResultForTitle(itemName1, 1).removeFavourite();

    searchPage = new SearchPage(context).load();
    results = searchPage.exactQuery(itemName2);
    assertTrue(results.doesResultExist(itemName2, 1));
    results.getResultForTitle(itemName2, 1).removeFavourite();
  }

  // TODO: OEQ-2610 re-enable it in new UI or write new test.
  @Test
  @OldUIOnly
  public void testVersionFavourites() {
    logon("AutoTest", "automated");
    String name = "testversion";
    String itemName = context.getFullName(name);
    String thisversion = "thisversion";
    String latestversion = "latestversion";

    // Add Item
    WizardPageTab wizard =
        new ContributePage(context).load().openWizard(GENERIC_TESTING_COLLECTION);
    wizard.editbox(1, itemName);
    wizard.save().publish();

    // Add to favourites and choose this version
    SearchPage searchPage = new SearchPage(context).load();
    ItemListPage results = searchPage.exactQuery(itemName);
    assertTrue(results.doesResultExist(itemName, 1));

    // Add it to favourites (Check that link changes)
    ItemSearchResult resultForTitle = results.getResultForTitle(itemName, 1);
    resultForTitle.addToFavourites().setTags(thisversion).setLatestVersion(false).clickAdd();

    // New version item
    wizard = SearchPage.searchAndView(context, itemName).adminTab().newVersion();
    wizard.save().publish();

    // Add it to favourites
    searchPage = new SearchPage(context).load();
    results = searchPage.exactQuery(itemName);
    assertTrue(results.doesResultExist(itemName, 1));

    // Add it to favourites (Check that link changes)
    resultForTitle = results.getResultForTitle(itemName, 1);
    resultForTitle.addToFavourites().setTags(latestversion).setLatestVersion(true).clickAdd();

    // Check favourites point to correct versions
    FavouriteItemsPage favs = new FavouritesPage(context).load().items();
    ItemListPage favresults = favs.search(thisversion);

    // Check that latest points to version 1
    assertTrue(favresults.getResult(1).viewSummary().getItemId().getVersion() == 1);

    favs = new FavouritesPage(context).load().items();
    favresults = favs.search(latestversion);

    // Check that latest points to version 2
    assertTrue(favresults.getResult(1).viewSummary().getItemId().getVersion() == 2);

    wizard = SearchPage.searchAndView(context, itemName).adminTab().newVersion();
    wizard.save().publish();

    favs = new FavouritesPage(context).load().items();
    favresults = favs.search(latestversion);

    // Check that latest points to version 3
    assertTrue(favresults.getResult(1).viewSummary().getItemId().getVersion() == 3);

    // Remove favourites
    favs = new FavouritesPage(context).load().items();
    favresults = favs.search(latestversion);
    favresults.getResultForTitle(itemName, 1).removeFavourite();
    favresults = favs.search(thisversion);
    favresults.getResultForTitle(itemName, 1).removeFavourite();
  }

  // TODO: OEQ-2610 re-enable it in new UI or write new test.
  @Test
  @OldUIOnly
  public void testNoResults() {
    logon("TLE_ADMINISTRATOR", testConfig.getAdminPassword());

    FavouriteItemsPage favs = new FavouritesPage(context).load().items();
    assertFalse(favs.hasResults());
  }

  // TODO: OEQ-2610 re-enable it in new UI or write new test.
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
}
