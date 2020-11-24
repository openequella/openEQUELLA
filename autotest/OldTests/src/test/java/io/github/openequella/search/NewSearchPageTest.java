package io.github.openequella.search;

import static org.testng.Assert.assertEquals;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import com.tle.webtests.test.AbstractSessionTest;
import io.github.openequella.pages.search.NewSearchPage;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.List;
import org.openqa.selenium.WebElement;
import org.testng.annotations.Test;
import testng.annotation.NewUIOnly;

@TestInstitution("facet")
public class NewSearchPageTest extends AbstractSessionTest {
  private NewSearchPage searchPage;

  @Override
  protected void prepareBrowserSession() {
    logon();
  }

  @Test(description = "open the new Search page and wait for initial search completed")
  @NewUIOnly
  public void initialSearch() {
    searchPage = new NewSearchPage(context).load();
    // The initial search should return 16 items.
    searchPage.waitForSearchCompleted(16);
  }

  @Test(dependsOnMethods = "initialSearch", description = "Search with a query and refine controls")
  @NewUIOnly
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
  @NewUIOnly
  public void openItemSummaryPage() {
    final String ITEM_TITLE = "Java (cloned)";
    SummaryPage summaryPage = searchPage.selectItem(ITEM_TITLE);
    assertEquals(summaryPage.getItemTitle(), ITEM_TITLE);
  }

  @Test(
      description = "Go back to the Search page from another page",
      dependsOnMethods = "openItemSummaryPage")
  @NewUIOnly
  public void backToSearchPage() {
    context.getDriver().navigate().back();
    searchPage.get();
    // Expect when going 'back' to the search page, the previous search
    // settings have been remembered.
    assertEquals(searchPage.getSearchBar().getAttribute("value"), "Java");
    searchPage.waitForSearchCompleted(1);
  }

  @Test(description = "Search with a low privileged user", dependsOnMethods = "backToSearchPage")
  @NewUIOnly
  public void searchWithLessACLS() {
    // This account can only access the Collection 'Hardware platforms' and items of this
    // Collection. But it has no access to attachments and comments.
    logon(AUTOTEST_LOW_PRIVILEGE_LOGON, AUTOTEST_PASSWD);

    searchPage = new NewSearchPage(context).load();
    searchPage.waitForSearchCompleted(7);
    searchPage.selectCollection("Hardware");
    searchPage.waitForSearchCompleted(7);
  }

  @Test(
      dependsOnMethods = "searchWithLessACLS",
      description = "Copy a Search link to clipboard, and use it")
  @NewUIOnly
  public void shareSearchUrl() throws IOException, UnsupportedFlavorException {
    searchPage = new NewSearchPage(context).load();
    searchPage.newSearch();
    searchPage.waitForSearchCompleted(7);
    // change a few controls on the page
    searchPage.expandRefineControlPanel();
    searchPage.selectSearchAttachments(false);
    searchPage.selectCollection("Hardware");
    searchPage.changeQuery("Zilog Z80");
    searchPage.waitForSearchCompleted(1);
    // copy search url to clipboard
    String sharedSearchUrl = searchPage.shareSearchLink();
    searchPage.newSearch();
    // navigate to shared search url
    context.getDriver().navigate().to(sharedSearchUrl);
    searchPage.waitForSearchCompleted(1);
    // the same values we set earlier should be selected in this search
    List<WebElement> selectedCollections = searchPage.getSelectedCollections();
    WebElement selectedSearchAttachment = searchPage.getSelectedSearchAttachmentValue();
    searchPage.expandRefineControlPanel();
    assertEquals(selectedCollections.size(), 1);
    assertEquals(selectedCollections.get(0).getText(), "Hardware Platforms");
    assertEquals(selectedSearchAttachment.getText(), "NO");
    assertEquals(searchPage.getSearchBar().getAttribute("value"), "Zilog Z80");
  }
}
