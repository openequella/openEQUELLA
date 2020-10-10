package io.github.openequella.search;

import static org.testng.Assert.assertEquals;

import com.tle.webtests.framework.ScreenshotTaker;
import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import com.tle.webtests.test.AbstractSessionTest;
import io.github.openequella.pages.search.NewSearchPage;
import org.testng.annotations.Test;

@TestInstitution("facet")
public class NewSearchPageTest extends AbstractSessionTest {
  private NewSearchPage searchPage;

  @Override
  protected void prepareBrowserSession() {
    logon();
  }

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
    // Search by date range quick options/
    searchPage.selectDateRangeQuickOption("All");
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
    SummaryPage summaryPage = searchPage.selectItem(ITEM_TITLE);
    assertEquals(summaryPage.getItemTitle(), ITEM_TITLE);
  }

  @Test(
      description = "Go back to the Search page from another page",
      dependsOnMethods = "openItemSummaryPage")
  public void backToSearchPage() {
    context.getDriver().navigate().back();
    ScreenshotTaker.takeScreenshot(
        context.getDriver(),
        context.getTestConfig().getScreenshotFolder(),
        "NewSearchPageTest - check Summary Page " + context.getTestConfig().isNewUI(),
        true);
    // todo: remove this when we can skip this test suite in Old UI mode.
    //    if(searchPage.usingNewUI()) {
    //      context.getDriver().navigate().refresh();
    //    }
    searchPage.get();
    // Expect when going 'back' to the search page, the previous search
    // settings have been remembered.
    assertEquals(searchPage.getSearchBar().getAttribute("value"), "Java1");
    searchPage.waitForSearchCompleted(1);
  }

  @Test(description = "Search with a low privileged user", dependsOnMethods = "backToSearchPage")
  public void searchWithLessACLS() {
    // This account can only access the Collection 'Hardware platforms' and items of this
    // Collection.
    // But it has no access to attachments and comments.
    logon(AUTOTEST_LOW_PRIVILEGE_LOGON, AUTOTEST_PASSWD);

    searchPage = new NewSearchPage(context).load();
    searchPage.waitForSearchCompleted(7);
    searchPage.selectCollection("Hardware");
    searchPage.waitForSearchCompleted(7);
  }
}
