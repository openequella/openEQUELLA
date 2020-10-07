package com.tle.webtests.test.searching;

import static org.testng.Assert.assertEquals;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.searching.NewSearchPage;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import com.tle.webtests.test.AbstractCleanupAutoTest;
import org.openqa.selenium.WebElement;
import org.testng.annotations.Test;

@TestInstitution("facet")
public class NewSearchPageTest extends AbstractCleanupAutoTest {
  private NewSearchPage searchPage;

  @Test(description = "open the new Search page and wait for initial search completed")
  public void initialSearch() {
    searchPage = new NewSearchPage(context).load();
    // The initial search should return 16 items.
    searchPage.waitForSearchCompleted(16);
  }

  @Test(dependsOnMethods = "initialSearch", description = "Search with a query and refine controls")
  public void searchByFilters() {
    searchPage.newSearch();
    // Search by Collections.
    searchPage.selectCollection("programming");
    searchPage.waitForSearchCompleted(9);
    // Expand the Refine control panel.
    searchPage.expandRefineControlPanel();
    // Search by Item status.
    searchPage.selectStatus(true);
    searchPage.waitForSearchCompleted(10);
    // Search by a query.
    searchPage.changeQuery("Java");
    searchPage.waitForSearchCompleted(4);
    // Exclude attachments in a search.
    searchPage.selectSearchAttachments(false);
    searchPage.waitForSearchCompleted(4);
    // Search by date ranges.
    searchPage.selectCustomDateRange("2020-10-01", "2020-10-06");
    searchPage.waitForSearchCompleted(1);
    // Search by an owner.
    searchPage.selectOwner("AutoTest");
    searchPage.waitForSearchCompleted(1);
  }

  @Test(description = "open an item's summary page", dependsOnMethods = "searchByFilters")
  public void openItemSummaryPage() {
    final String ITEM_TITLE = "Java (cloned)";
    WebElement titleLink = searchPage.getItemTitleLink(ITEM_TITLE);
    titleLink.click();
    SummaryPage summary = new SummaryPage(context).get();
    assertEquals(summary.getItemTitle(), ITEM_TITLE);
  }

  @Test(
      description = "Go back to the Search page from another page",
      dependsOnMethods = "openItemSummaryPage")
  public void backToSearchPage() {
    context.getDriver().navigate().back();
    WebElement searchBar = searchPage.getSearchBar();
    // Expect when going 'back' to the search page, the previous search
    // settings have been remembered.
    assertEquals(searchBar.getAttribute("value"), "Java");
    searchPage.waitForSearchCompleted(1);
  }

  @Override
  protected void cleanupAfterClass() throws Exception {
    // This test does not need to clean anything.
  }
}
