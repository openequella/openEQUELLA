package com.tle.webtests.test.searching;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.searching.FavouritesPage;
import com.tle.webtests.pageobject.searching.ItemListPage;
import com.tle.webtests.pageobject.searching.SearchPage;
import com.tle.webtests.pageobject.wizard.ConfirmationDialog;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.test.AbstractCleanupTest;
import java.lang.reflect.Method;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

@TestInstitution("workflow")
public class GuidedSearchingTest extends AbstractCleanupTest {

  private static final String COLLECTION3 = "Move to Live During Moderation";
  private static final String COLLECTION1 = "Basic collection for searching";
  private static final String COLLECTION2 = "No Workflow";
  private static final String ITEM1_NAME = "Searching item 1 - this one is a searching item";
  private static final String ITEM2_NAME = "Searching item 2 - this ones from no workflow";
  private static final String ITEM3_NAME =
      "Searching item 3 - this one is from the move live during moderation";

  public GuidedSearchingTest() {
    setDeleteCredentials("admin", "``````");
  }

  @Override
  public void setupSubcontext(Method testMethod) {
    // no subcontext
  }

  @DataProvider(name = "contributeData", parallel = false)
  public Object[][] contributeData() {
    return new Object[][] {
      {COLLECTION1, ITEM1_NAME, false},
      {COLLECTION2, ITEM2_NAME, false},
      {COLLECTION3, ITEM3_NAME, true},
    };
  }

  @Test(dataProvider = "contributeData")
  public void contributeSearchItems(String wizard, String item, boolean submit) {
    logon("admin", "``````");
    WizardPageTab wizardPage = new ContributePage(context).load().openWizard(wizard);
    wizardPage.editbox(1, context.getFullName(item));
    ConfirmationDialog confirm = wizardPage.save();
    if (submit) {
      confirm.submit();
    } else {
      confirm.publish();
    }
  }

  @Test(dependsOnMethods = "contributeSearchItems")
  public void searchingForGuides() {
    /* Login as the SimpleModerator and do a few things...
     *
     */
    logon("SimpleModerator", "``````");
    String prefixQuery = "+" + context.getNamePrefix();
    SearchPage searchPage = new SearchPage(context).load();
    searchPage.search(prefixQuery);

    searchPage.saveSearch("save search");
    assertItems(searchPage.results(), true, true, false);

    // By including the non live items, we should see the extra item that's
    // in moderation.
    searchPage.setIncludeNonLive(true);
    assertItems(searchPage.results(), true, true, true);

    // Try a guided search and add all the collections to be searchable.
    searchPage = new SearchPage(context).load();
    searchPage.setWithinAll();
    searchPage.setQuery(prefixQuery);
    searchPage.setIncludeNonLive(false);
    assertItems(searchPage.search(), true, true, false);
    assertItems(searchPage.setIncludeNonLive(true).results(), true, true, true);

    // Try another guided search and add the No Workflow collection
    searchPage = new SearchPage(context).load();
    searchPage.setIncludeNonLive(false);
    searchPage.setWithinCollection(COLLECTION2);
    searchPage.setQuery(prefixQuery);
    searchPage.search();
    // You should only see the second Searching Item, the one from the No
    // workflow collection.
    assertItems(searchPage.results(), false, true, false);

    // Try another guided search and add the move to live during moderation
    // collection.
    searchPage = new SearchPage(context).load();
    searchPage.setWithinCollection(COLLECTION3);
    searchPage.setQuery(prefixQuery);
    assertFalse(searchPage.search().isResultsAvailable());
    // Save here instead, saved searches used to not save the status of the "include non live"
    // checkbox
    searchPage.saveSearch("live item search");
    // Add the live items and the third item should appear.
    assertItems(searchPage.setIncludeNonLive(true).results(), false, false, true);

    // Do another guided search and add the basic searching collection.
    searchPage = new SearchPage(context).load();
    searchPage.setIncludeNonLive(false);
    searchPage = searchPage.setWithinCollection(COLLECTION1);
    searchPage.setQuery(prefixQuery);
    searchPage.search();
    // You should see the first item this time.
    assertItems(searchPage.results(), true, false, false);
  }

  private void assertItemVisibility(ItemListPage results, String name, boolean b) {
    String fullName = context.getFullName(name);
    assertEquals(
        results.doesResultExist(fullName),
        b,
        "Expected " + fullName + " to be " + (b ? "found" : "missing"));
  }

  private void assertItems(ItemListPage results, boolean item1, boolean item2, boolean item3) {
    assertItemVisibility(results, ITEM1_NAME, item1);
    assertItemVisibility(results, ITEM2_NAME, item2);
    assertItemVisibility(results, ITEM3_NAME, item3);
  }

  @Test(dependsOnMethods = "searchingForGuides")
  public void testSavedSearches() {
    // Login as the SimpleModerator to do some searching.
    logon("SimpleModerator", "``````");

    selectSearch("save search");
    assertItems(new SearchPage(context).load().results(), true, true, false);

    selectSearch("live item search");
    SearchPage guidedSearchPage = new SearchPage(context).get();
    assertFalse(guidedSearchPage.hasResults());
    assertItems(guidedSearchPage.setIncludeNonLive(true).results(), false, false, true);

    deleteSearch("save search");
    deleteSearch("live item search");
  }

  private void selectSearchInOldUi(String searchName) {
    new FavouritesPage(context).load().searches().open(searchName);
  }

  private void selectSearchInNewUi(String searchName) {
    io.github.openequella.pages.favourites.FavouritesPage favouritePage =
        new io.github.openequella.pages.favourites.FavouritesPage(context).load();
    favouritePage.selectFavouritesSearchesType();
    favouritePage.selectSearch(searchName);
  }

  private void selectSearch(String searchName) {
    if (testConfig.isNewUI()) {
      selectSearchInNewUi(searchName);
    } else {
      selectSearchInOldUi(searchName);
    }
  }

  private void deleteSearchInOldUi(String searchName) {
    new FavouritesPage(context).load().searches().delete(searchName);
  }

  private void deleteSearchInNewUi(String searchName) {
    io.github.openequella.pages.favourites.FavouritesPage favouritePage =
        new io.github.openequella.pages.favourites.FavouritesPage(context).load();
    favouritePage.selectFavouritesSearchesType();
    favouritePage.removeFromFavourites(searchName);
  }

  private void deleteSearch(String searchName) {
    if (testConfig.isNewUI()) {
      deleteSearchInNewUi(searchName);
    } else {
      deleteSearchInOldUi(searchName);
    }
  }
}
