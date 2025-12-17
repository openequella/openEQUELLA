package com.tle.webtests.test.searching;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.searching.FavouriteSearchesPage;
import com.tle.webtests.pageobject.searching.FavouritesPage;
import com.tle.webtests.pageobject.searching.PowerSearchPage;
import com.tle.webtests.pageobject.searching.SearchPage;
import com.tle.webtests.test.AbstractCleanupTest;
import org.testng.Assert;
import org.testng.annotations.Test;

@TestInstitution("dinuk")
public class FavouritesPowerSearchIntegration extends AbstractCleanupTest {

  private static final String POWER_SEARCH_NAME = "MY SAVED POWER SEARCH";

  @Test
  public void addPowerSearchtoFavourites() {
    logon("AutoTest", "automated");
    // Enter Search Page
    SearchPage searchPage = new SearchPage(context).load();

    // Enter Power Search Screen
    PowerSearchPage powerSearchPage = searchPage.setWithinPowerSearch("Pow Search Adv");
    powerSearchPage.editbox(3).setText("Baskervills");
    powerSearchPage.editbox(4).setText("Bourne");
    searchPage = powerSearchPage.search();

    // Check the Search Results
    Assert.assertTrue(searchPage.results().doesResultExist("Contribution to Authors"));
    Assert.assertFalse(searchPage.results().doesResultExist("Contribution to Authors A"));

    // Save Search to Favorites
    searchPage.saveSearch(POWER_SEARCH_NAME);

    // Access Favorites Search Page
    if (testConfig.isNewUI()) {
      io.github.openequella.pages.favourites.FavouritesPage favouritePage =
          new io.github.openequella.pages.favourites.FavouritesPage(context).load();
      favouritePage.selectFavouritesSearchesType();
      Assert.assertTrue(favouritePage.hasSearch(POWER_SEARCH_NAME));
      favouritePage.selectSearch(POWER_SEARCH_NAME);
    } else {
      FavouritesPage favouritePage = new FavouritesPage(context).load();
      Assert.assertTrue(favouritePage.searches().results().doesResultExist(POWER_SEARCH_NAME));
      favouritePage.accessSavedSearches(POWER_SEARCH_NAME);
    }

    // Check Power Search after Accessing from Favorites
    Assert.assertTrue(searchPage.hasResults());
    Assert.assertTrue(searchPage.results().doesResultExist("Contribution to Authors"));
    Assert.assertFalse(searchPage.results().doesResultExist("Contribution to Authors A"));
  }

  @Override
  protected void cleanupAfterClass() throws Exception {
    logon("AutoTest", "automated");
    FavouriteSearchesPage favouritePage = new FavouritesPage(context).load().searches();
    if (favouritePage.results().doesResultExist(POWER_SEARCH_NAME)) {
      favouritePage.deleteAllNamed(POWER_SEARCH_NAME);
    }
  }
}
