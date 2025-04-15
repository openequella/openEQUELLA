package io.github.openequella.search;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.IntegrationTesterPage;
import com.tle.webtests.pageobject.SettingsPage;
import com.tle.webtests.pageobject.selection.SelectionSession;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import com.tle.webtests.test.AbstractSessionTest;
import io.github.openequella.pages.search.NewSearchPage;
import org.openqa.selenium.By;
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
    searchPage = new NewSearchPage(context).load();
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
    searchPage.verifyExportButtonNotDisplayed();
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
    searchPage.expandAttachments(searchTerm);
    searchPage.verifyAttachmentDisplayed(attachmentTitle);

    // Then, logon as AutoTest (who doesn't have VIEW_RESTRICTED_ATTACHMENTS)
    logon(AUTOTEST_LOGON, AUTOTEST_PASSWD);
    searchPage = new NewSearchPage(context).load();

    // and verify that the attachment doesn't show when the item appears in a search.
    searchPage.changeQuery(searchTerm);
    searchPage.waitForSearchCompleted(1);
    searchPage.expandAttachments(searchTerm);
    searchPage.verifyAttachmentNotDisplayed(attachmentTitle);
  }

  @Test(description = "Export search result with a Collection")
  @NewUIOnly
  public void export() {
    final String COLLECTION_ERROR_MESSAGE = "Download limited to one collection.";
    searchPage = new NewSearchPage(context).load();

    searchPage.newSearch();
    searchPage.export();
    // Show snackbar to indicate the Collection selection error.
    searchPage.verifySnackbarMessage(COLLECTION_ERROR_MESSAGE);

    searchPage.selectCollection("programming");
    searchPage.waitForSearchCompleted(9);
    searchPage.export();
    // Show a Tick icon to indicate an export is done.
    assertTrue(searchPage.getExportDoneButton().isDisplayed());

    searchPage.selectCollection("hardware");
    searchPage.waitForSearchCompleted(16);
    searchPage.export();
    // Show snackbar again to indicate the Collection selection error.
    searchPage.verifySnackbarMessage(COLLECTION_ERROR_MESSAGE);
  }

  // To ensure that new search page bundled by Parcel 2 can be rendered correctly within the
  // selection session.
  @Test(description = "Show new search page in selection session")
  @NewUIOnly
  public void testSelection() {
    logon("TLE_ADMINISTRATOR", testConfig.getAdminPassword());
    new SettingsPage(context).load().setNewSearchUI(true);

    IntegrationTesterPage itp = new IntegrationTesterPage(context, "test", "test").load();
    itp.getSignonUrl("searchResources", "AutoTest", "", "", true);
    SelectionSession session = itp.clickPostToUrlButton(new SelectionSession(context));

    // make sure we can see the new search page
    assertTrue(
        session.isVisible(By.xpath("//*[contains(text(),'Search result')]")),
        "search Page is not loaded");

    new SettingsPage(context).load().setNewSearchUI(false);
    itp.stopIntegServer.apply();
  }
}
