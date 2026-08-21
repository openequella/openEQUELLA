package com.tle.webtests.test.searching.manage;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.searching.FavouriteSearchesPage;
import com.tle.webtests.pageobject.searching.FavouritesPage;
import com.tle.webtests.pageobject.searching.ItemAdminPage;
import com.tle.webtests.pageobject.searching.ItemSearchResult;
import com.tle.webtests.pageobject.searching.WhereQueryPage;
import com.tle.webtests.test.AbstractCleanupAutoTest;
import java.text.ParseException;
import java.util.List;
import org.testng.annotations.Test;

@TestInstitution("manageresources")
public class ManageResourcesFavouritesTest extends AbstractCleanupAutoTest {
  private static final String OTHER_COLLECTION_ITEM = "Something in a different collection";
  private static final String ORIGINAL_COLLECTION = "Generic Testing Collection";
  private static final String OLD_ITEM = "Predates the grassy knoll";
  private static final String NEW_ITEM = "Item with attached pic";
  private static final String PREFIX = "ManageResourcesFavouritesTest ";
  private static final String OTHER_COLLECTION = "Browse By Collection";

  @Test
  public void createFavourites() throws ParseException {
    ItemAdminPage iap = new ItemAdminPage(context).load();

    // Query is maintained
    iap.setWithinCollection(ORIGINAL_COLLECTION).exactQuery(OLD_ITEM);
    assertOneResult(iap, OLD_ITEM);
    iap.saveSearch(PREFIX + OLD_ITEM);

    // Filters are maintained
    iap.setWithinCollection(ORIGINAL_COLLECTION)
        .filterByDates("2012-01-01", "2012-12-30")
        .search("");
    assertOneResult(iap, NEW_ITEM);
    iap.saveSearch(PREFIX + NEW_ITEM);

    // Where clause is maintained
    iap = iap.clearFilters();
    iap.setWithinCollection(ORIGINAL_COLLECTION);
    WhereQueryPage wqp = iap.editWhere();
    wqp.setPredicate("WHERE");
    wqp.setWherePath("/xml/calendar");
    wqp.setWhereOperand(">");
    wqp.setWhereValue("2011-01-01");
    wqp.executeCriterion();
    assertOneResult(iap, NEW_ITEM);
    iap.saveSearch(PREFIX + NEW_ITEM + "2");

    // Collection selection is maintained
    iap.clearQuery().setWithinCollection(OTHER_COLLECTION);
    iap.search(OTHER_COLLECTION_ITEM);

    assertTrue(iap.results().doesResultExist(OTHER_COLLECTION_ITEM));
    iap.saveSearch(PREFIX + OTHER_COLLECTION_ITEM);
  }

  @Test(dependsOnMethods = "createFavourites")
  public void testFavourites() throws ParseException {
    checkFavouriteSearch(PREFIX + OLD_ITEM, OLD_ITEM);
    checkFavouriteSearch(PREFIX + NEW_ITEM, NEW_ITEM);
    checkFavouriteSearch(PREFIX + NEW_ITEM + "2", NEW_ITEM);
    checkFavouriteSearch(PREFIX + OTHER_COLLECTION_ITEM, OTHER_COLLECTION_ITEM);
  }

  public void assertOneResult(ItemAdminPage iap, String name) {
    List<ItemSearchResult> results = iap.results().getResults();
    assertEquals(results.size(), 1, "Number of returned results was wrong");
    assertEquals(results.get(0).getTitle(), name, "Wrong result");
  }

  @Override
  protected void cleanupAfterClass() throws Exception {
    FavouriteSearchesPage fsp = new FavouritesPage(context).load().searches();
    fsp.deleteAllNamed(
        PREFIX + OLD_ITEM,
        PREFIX + NEW_ITEM,
        PREFIX + NEW_ITEM + "2",
        PREFIX + OTHER_COLLECTION_ITEM);
  }

  private ItemAdminPage openSearchPageInOldUI(String searchName) {
    FavouriteSearchesPage fsp = new FavouritesPage(context).load().searches();
    return fsp.open(searchName, new ItemAdminPage(context));
  }

  private ItemAdminPage openSearchPageInNewUI(String searchName) {
    io.github.openequella.pages.favourites.FavouritesPage favouritesPage =
        new io.github.openequella.pages.favourites.FavouritesPage(context).load();
    favouritesPage.selectFavouritesSearchesType();
    favouritesPage.selectSearch(searchName);

    return new ItemAdminPage(context);
  }

  // Make sure the saved favourite item manage search page can be accessed and returns the correct
  // result.
  private void checkFavouriteSearch(String searchName, String expectedItemName) {
    ItemAdminPage itemAdminPage =
        testConfig.isNewUI()
            ? openSearchPageInNewUI(searchName)
            : openSearchPageInOldUI(searchName);
    assertOneResult(itemAdminPage, expectedItemName);
  }
}
