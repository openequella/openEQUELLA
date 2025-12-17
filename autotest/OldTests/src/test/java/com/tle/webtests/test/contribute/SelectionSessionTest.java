package com.tle.webtests.test.contribute;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.IntegrationTesterPage;
import com.tle.webtests.pageobject.IntegrationTesterReturnPage;
import com.tle.webtests.pageobject.ReceiptPage;
import com.tle.webtests.pageobject.SettingsPage;
import com.tle.webtests.pageobject.searching.FavouriteSearchesPage;
import com.tle.webtests.pageobject.searching.FavouritesPage;
import com.tle.webtests.pageobject.searching.ItemListPage;
import com.tle.webtests.pageobject.searching.ItemSearchResult;
import com.tle.webtests.pageobject.searching.SearchPage;
import com.tle.webtests.pageobject.selection.SelectionSession;
import com.tle.webtests.pageobject.settings.SelectionSessionSettingsPage;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.test.AbstractIntegrationTest;
import com.tle.webtests.test.files.Attachments;
import java.util.List;
import org.openqa.selenium.WebElement;
import org.testng.annotations.Test;

@TestInstitution("contribute")
public class SelectionSessionTest extends AbstractIntegrationTest {
  private static String USERNAME = "AutoTest";
  private static String PASSWORD = "automated";
  private static String SHAREDID = "contribute";
  private static String SECRET = "contribute";
  private static String ACTION_SELECT_OR_ADD = "selectOrAdd";
  private static String ACTION_STRUCTURED = "structured";

  public SelectionSessionTest() {
    setDeleteCredentials(USERNAME, PASSWORD);
  }

  // http://dev.equella.com/issues/6495
  @Test
  public void searchResourcesActionTest() {
    IntegrationTesterPage itp = new IntegrationTesterPage(context, SHAREDID, SECRET).load();
    itp.getSignonUrl("searchResources", USERNAME, "", "", true);

    SelectionSession session = itp.clickPostToUrlButton(new SelectionSession(context));

    SearchPage search = session.getSearchPage();
    ItemListPage results = search.search("CancelWizardTest");

    assertTrue(results.isResultsAvailable(), "results not returned");
    List<ItemSearchResult> resultList = results.getResults();
    assertEquals(
        resultList.get(0).getTitle(),
        "CancelWizardTest an item",
        "Expected search result 'CancelWizardTest an item' was not found");
  }

  @Test
  public void contributeActionTest() {
    IntegrationTesterPage itp = new IntegrationTesterPage(context, SHAREDID, SECRET).load();
    itp.getSignonUrl("contribute", USERNAME, "", "", true);
    SelectionSession session = itp.clickPostToUrlButton(new SelectionSession(context));

    // just make sure we can see the contribute page
    ContributePage contrib = session.getContributePage();
    assertTrue(
        contrib.hasCollection("Basic Attachments"),
        "'Basic Attachments' not found on contribute page");

    // TODO: more stuff?
  }

  @Test
  public void quickUploadTest() {
    String ATTACHMENT = "SelectionSessionTest - google.png";
    String ATTACHMENT2 = "SelectionSessionTest - google2.png";

    IntegrationTesterPage itp = new IntegrationTesterPage(context, SHAREDID, SECRET).load();
    itp.getSignonUrl(ACTION_SELECT_OR_ADD, USERNAME, "", "", true);
    SelectionSession session = itp.clickPostToUrlButton(new SelectionSession(context));

    IntegrationTesterReturnPage returnPage =
        session.quickContribute(
            Attachments.get(ATTACHMENT), new IntegrationTesterReturnPage(context));
    assertTrue(returnPage.returnedRow("links").contains(ATTACHMENT));
    assertTrue(returnPage.returnedRow("method").contains("showReturn"));

    itp = new IntegrationTesterPage(context, SHAREDID, SECRET).load();
    itp.getSignonUrl(ACTION_SELECT_OR_ADD, USERNAME, "", "", false);
    session = itp.clickPostToUrlButton(new SelectionSession(context));

    returnPage =
        session.quickContribute(
            Attachments.get(ATTACHMENT), new IntegrationTesterReturnPage(context));
    assertTrue(returnPage.returnedRow("name").contains(ATTACHMENT));
    assertTrue(returnPage.returnedRow("method").contains("showReturn"));

    itp = new IntegrationTesterPage(context, SHAREDID, SECRET).load();
    itp.getSignonUrl(ACTION_SELECT_OR_ADD, USERNAME, "", "", false);
    session = itp.clickPostToUrlButton(new SelectionSession(context));

    returnPage =
        session.quickContribute(
            Attachments.get(ATTACHMENT2), new IntegrationTesterReturnPage(context));
    assertFalse(returnPage.returnedRow("name").contains(ATTACHMENT2));
    assertTrue(returnPage.returnedRow("name").contains(ATTACHMENT));
    assertTrue(returnPage.returnedRow("method").contains("showReturn"));
  }

  @Test
  public void selectionPortletTest() {
    String fullName = context.getFullName("favourite me");

    String user = "SelectionsUser";
    String pass = "``````";

    logon(user, pass);
    WizardPageTab wizard = new ContributePage(context).load().openWizard("Basic Items");

    wizard.editbox(1, fullName);
    SummaryPage view = wizard.save().publish();
    view.addToFavourites().clickAdd();

    SearchPage searchPage = new SearchPage(context).load();
    ItemListPage listPage = searchPage.exactQuery(fullName);
    assertTrue(listPage.isResultsAvailable());
    searchPage.saveSearch("fav search");

    IntegrationTesterPage itp = new IntegrationTesterPage(context, SHAREDID, SECRET).load();
    itp.getSignonUrl(ACTION_SELECT_OR_ADD, user, "", "");
    SelectionSession session = itp.clickPostToUrlButton(new SelectionSession(context));

    assertTrue(session.favItemExists(fullName));
    assertTrue(session.favSearchExists("fav search"));
    assertTrue(session.recentContributionExists(fullName));
    assertFalse(session.recentSelectedExists(fullName));

    session
        .homeExactSearch(fullName)
        .getResultForTitle(fullName, 1)
        .viewSummary()
        .selectItem(new IntegrationTesterReturnPage(context));

    itp = new IntegrationTesterPage(context, SHAREDID, SECRET).load();
    itp.getSignonUrl(ACTION_SELECT_OR_ADD, user, "", "");
    session = itp.clickPostToUrlButton(new SelectionSession(context));

    assertTrue(session.favItemExists(fullName));
    assertTrue(session.favSearchExists("fav search"));
    assertTrue(session.recentContributionExists(fullName));
    assertTrue(session.recentSelectedExists(fullName));
  }

  @Test
  public void displayTemplate() {
    IntegrationTesterPage itp = new IntegrationTesterPage(context, SHAREDID, SECRET).load();
    itp.getSignonUrl(ACTION_SELECT_OR_ADD, USERNAME, "", "", true);
    SelectionSession session = itp.clickPostToUrlButton(new SelectionSession(context));
    WizardPageTab wizard = session.contribute().openWizard("XSLT Display Template");
    String fullName = context.getFullName("template");
    wizard.editbox(1, fullName);
    wizard.save().publish();
    assertTrue(isTextPresent(fullName));
    assertTrue(isTextPresent("static"));
    assertTrue(isTextPresent("Generated via a display template"));

    logon(USERNAME, PASSWORD);
    new SearchPage(context).load().exactQuery(fullName).viewFromTitle(fullName);
    assertTrue(isTextPresent(fullName));
    assertTrue(isTextPresent("static"));
    assertTrue(isTextPresent("Generated via a display template"));
  }

  @Test
  public void selectSummaryPageButtonSettingTest() {
    logon(USERNAME, PASSWORD);
    SettingsPage settingsPage = new SettingsPage(context).load();
    SelectionSessionSettingsPage settingPage = settingsPage.selectionSessionSettingsPage();
    settingPage.selectDisableBox();
    settingPage.save();

    IntegrationTesterPage itp = new IntegrationTesterPage(context, SHAREDID, SECRET).load();
    itp.getSignonUrl(ACTION_STRUCTURED, USERNAME, "", "", true);
    SelectionSession session = itp.clickPostToUrlButton(new SelectionSession(context));
    SearchPage searchPage = session.getSearchPage();

    assertFalse(searchPage.isSelectButtonExist());

    settingsPage = new SettingsPage(context).load();
    settingPage = settingsPage.selectionSessionSettingsPage();
    settingPage.unselectDisableBox();
    settingPage.save();

    itp = new IntegrationTesterPage(context, SHAREDID, SECRET).load();
    itp.getSignonUrl(ACTION_STRUCTURED, USERNAME, "", "", true);
    session = itp.clickPostToUrlButton(new SelectionSession(context));
    searchPage = session.getSearchPage();
    assertTrue(searchPage.isSelectButtonExist());
  }

  @Test
  public void saveSearchAndShareSearchTest() {
    IntegrationTesterPage itp = new IntegrationTesterPage(context, SHAREDID, SECRET).load();
    itp.getSignonUrl(ACTION_STRUCTURED, USERNAME, "", "", true);
    SelectionSession session = itp.clickPostToUrlButton(new SelectionSession(context));
    SearchPage searchPage = session.getSearchPage();
    searchPage.search("BasicViewerImage");
    String searchName = "A search";
    searchPage = searchPage.saveSearchToFavourites(searchName);
    ReceiptPage.waiter("Successfully added this search to your favourites", searchPage).get();

    assertEquals(
        searchPage.getShareSearchUrl(),
        context.getBaseUrl()
            + "searching.do?in=all&q=BasicViewerImage&type=standard&sort=rank&dr=AFTER");

    if (testConfig.isNewUI()) {
      io.github.openequella.pages.favourites.FavouritesPage page =
          new io.github.openequella.pages.favourites.FavouritesPage(context).load();
      page.selectFavouritesSearchesType();
      assertTrue(page.hasSearch(searchName));
      page.removeSearch(searchName);
    } else {
      FavouriteSearchesPage searches = new FavouritesPage(context).load().searches();
      assertTrue(searches.results().doesResultExist(searchName));
      searches.delete(searchName);
    }
  }

  @Test
  public void integrationScreenTopLinkMenuTest() {
    IntegrationTesterPage itp = new IntegrationTesterPage(context, SHAREDID, SECRET).load();
    itp.getSignonUrl(ACTION_STRUCTURED, USERNAME, "", "", true);
    SelectionSession session = itp.clickPostToUrlButton(new SelectionSession(context));
    List<WebElement> topLinkMenu = session.getTopLinkMenu();
    assertEquals(topLinkMenu.size(), 5);
    assertTrue(topLinkMenu.get(0).getText().equalsIgnoreCase("Search"));
    assertTrue(topLinkMenu.get(1).getText().equalsIgnoreCase("Browse"));
    assertTrue(topLinkMenu.get(2).getText().equalsIgnoreCase("Favourites"));
    assertTrue(topLinkMenu.get(3).getText().equalsIgnoreCase("My resources"));
    assertTrue(topLinkMenu.get(4).getText().equalsIgnoreCase("Contribute"));
  }

  @Override
  protected void cleanupAfterClass() throws Exception {
    super.cleanupAfterClass();
    FavouriteSearchesPage searches = new FavouritesPage(context).load().searches();
    if (searches.results().doesResultExist("fav search")) {
      searches.delete("fav search");
    }
  }
}
