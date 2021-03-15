package io.github.openequella.search;

import static org.testng.Assert.assertEquals;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import com.tle.webtests.test.AbstractSessionTest;
import io.github.openequella.pages.search.NewSearchPage;
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
      description =
          "Search with two differently privileged users when there is a restricted attachment")
  @NewUIOnly
  public void searchWithRestrictedAttachments() {
    String attachmentTitle = "https://en.wikipedia.org/wiki/Itanium";
    String searchTerm = "Itanium";
    // The Item 'Itanium' has been set with a restricted attachment.

    // To test, logon as TLE_ADMINISTRATOR (who inherently has VIEW_RESTRICTED_ATTACHMENTS)
    logon("TLE_ADMINISTRATOR", testConfig.getAdminPassword());
    searchPage = new NewSearchPage(context).load();

    // and verify the attachment appears when the item appears in a search.
    searchPage.changeQuery(searchTerm);
    searchPage.waitForSearchCompleted(1);
    searchPage.verifyAttachmentDisplayed(attachmentTitle);

    // Then, logon as AutoTest (who doesn't have VIEW_RESTRICTED_ATTACHMENTS)
    logon(AUTOTEST_LOGON, AUTOTEST_PASSWD);
    searchPage = new NewSearchPage(context).load();

    // and verify that the attachment doesn't show when the item appears in a search.
    searchPage.changeQuery(searchTerm);
    searchPage.waitForSearchCompleted(1);
    searchPage.verifyAttachmentNotDisplayed(attachmentTitle);
  }
}
