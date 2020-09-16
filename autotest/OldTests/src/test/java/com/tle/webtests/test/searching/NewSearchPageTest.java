package com.tle.webtests.test.searching;

import static org.testng.Assert.assertEquals;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.searching.NewSearchPage;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import com.tle.webtests.test.AbstractCleanupAutoTest;
import org.openqa.selenium.WebElement;
import org.testng.annotations.Test;

@TestInstitution("fiveo")
public class NewSearchPageTest extends AbstractCleanupAutoTest {
  private NewSearchPage searchPage;

  @Test(description = "open the new Search page and wait for initial search completed")
  public void initialSearch() {
    searchPage = new NewSearchPage(context).load();
    // The initial search should return 10 items.
    searchPage.waitForSearchCompleted(45);
  }

  @Test(dependsOnMethods = "initialSearch", description = "Search with a query and refine controls")
  public void searchByFilters() {
    searchPage.newSearch();
    // Search by Collections.
    searchPage.selectCollection("Basic Items", "DRM Attachment Only");
    searchPage.waitForSearchCompleted(21);
    // Search by date ranges.
    searchPage.selectDateRangeQuickOption("Today");
    searchPage.waitForSearchCompleted(0);
    searchPage.selectDateRangeQuickOption("All");
    searchPage.waitForSearchCompleted(21);
    searchPage.selectCustomDateRange("2011-03-24", "2020-09-16");
    searchPage.waitForSearchCompleted(14);
    // Search by a query.
    searchPage.changeQuery("item");
    searchPage.waitForSearchCompleted(8);
    // Search by Item status.
    searchPage.selectStatus(true);
    searchPage.waitForSearchCompleted(9);
    // Exclude attachments in a search.
    searchPage.selectSearchAttachments(false);
    searchPage.waitForSearchCompleted(8);
    // Search by an owner.
    searchPage.selectOwner("DoNotUse");
    searchPage.waitForSearchCompleted(1);
  }

  @Test(description = "open an item's summary page", dependsOnMethods = "searchByFilters")
  public void openItemSummaryPage() {
    final String ITEM_TITLE = "SearchFilters - Basic Item";
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
    assertEquals(searchBar.getAttribute("value"), "item");
    searchPage.waitForSearchCompleted(1);
  }
}
