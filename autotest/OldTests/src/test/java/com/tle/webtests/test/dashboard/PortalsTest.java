package com.tle.webtests.test.dashboard;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.HomePage;
import com.tle.webtests.pageobject.portal.BrowsePortalEditPage;
import com.tle.webtests.pageobject.portal.BrowsePortalSection;
import com.tle.webtests.pageobject.portal.DashboardAdminPage;
import com.tle.webtests.pageobject.portal.FavPortalEditPage;
import com.tle.webtests.pageobject.portal.FavPortalSection;
import com.tle.webtests.pageobject.portal.FreemarkerPortalEditPage;
import com.tle.webtests.pageobject.portal.HtmlPortalEditPage;
import com.tle.webtests.pageobject.portal.HtmlPortalSection;
import com.tle.webtests.pageobject.portal.MenuSection;
import com.tle.webtests.pageobject.portal.MyResourcesPortalEditPage;
import com.tle.webtests.pageobject.portal.MyResourcesPortalSection;
import com.tle.webtests.pageobject.portal.RecentContributionsEditPage;
import com.tle.webtests.pageobject.portal.RecentContributionsSection;
import com.tle.webtests.pageobject.portal.RssPortalEditPage;
import com.tle.webtests.pageobject.portal.RssPortalSection;
import com.tle.webtests.pageobject.portal.SearchPortalEditPage;
import com.tle.webtests.pageobject.portal.SearchPortalSection;
import com.tle.webtests.pageobject.portal.TaskStatisticsPortalEditPage;
import com.tle.webtests.pageobject.portal.TaskStatisticsPortalSection;
import com.tle.webtests.pageobject.searching.FavouriteSearchesPage;
import com.tle.webtests.pageobject.searching.FavouritesPage;
import com.tle.webtests.pageobject.searching.SearchPage;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.test.AbstractCleanupTest;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;

@TestInstitution("vanilla")
public class PortalsTest extends AbstractCleanupTest {
  public PortalsTest() {
    setDeleteCredentials("AutoTest", "automated");
  }

  @Test
  public void testMinimisable() {
    String noMinMax = context.getFullName("noMinMax");
    createPortal(noMinMax, false, true, false);
    testPortal(noMinMax, false, true);
  }

  @Test
  public void testClosable() {
    String noClose = context.getFullName("noClose");
    createPortal(noClose, true, false, false);
    testPortal(noClose, true, false);
  }

  @Test
  public void testMinimisableClosable() {
    String noMinMaxClose = context.getFullName("noMinMaxClose");
    createPortal(noMinMaxClose, false, false, false);
    testPortal(noMinMaxClose, false, false);
  }

  @Test
  public void testDisabled() {
    String disabled = context.getFullName("disabled");
    createPortal(disabled, true, true, true);
    HomePage home = logonToHome("portlettest2", "``````");
    assertFalse(home.portalExists(disabled));
  }

  @Test
  public void testMinimisingAndClosing() {
    String enabled = context.getFullName("enabled");
    createPortal(enabled, true, true, false);
    String portalText = "Test portal: " + enabled;

    HomePage home = logonToHome("portlettest2", "``````");
    assertTrue(home.portalExists(enabled));
    assertTrue(isTextPresent(portalText));

    HtmlPortalSection html = new HtmlPortalSection(context, enabled).get();
    html.minMax();
    dash();
    assertFalse(isTextPresent(portalText));
    html.minMax();
    dash();
    assertTrue(isTextPresent(portalText));

    home = logonToHome("portlettest2", "``````");
    assertTrue(home.portalExists(enabled), "Portal '" + enabled + "' not found");
    assertTrue(isTextPresent(portalText));

    html = new HtmlPortalSection(context, enabled).get();
    html.delete();
    assertFalse(home.portalExists(enabled));
    home.restoreAll();

    assertTrue(home.portalExists(enabled));
    assertTrue(isTextPresent(portalText));
  }

  private void createPortal(String name, boolean canMinMan, boolean canClose, boolean disabled) {
    logonToHome("portlettest1", "``````");
    DashboardAdminPage adminPage = new DashboardAdminPage(context).load();
    String portalText = "Test portal: " + name;

    HtmlPortalEditPage portal = new HtmlPortalEditPage(context);
    portal = adminPage.addPortal(portal);

    portal.setTitle(name);
    portal.setText(portalText);
    portal.showForExpression("U:325c1203-65dc-c3c5-d815-e78418e08b38 "); // portlettest2
    portal.setUsersCanClose(canClose);
    portal.setUsersCanMin(canMinMan);
    portal.setDisabled(disabled);
    portal.save(adminPage);

    HomePage home = new MenuSection(context).home();
    assertTrue(disabled || home.portalExists(name));
  }

  private void testPortal(String name, boolean canMinMan, boolean canClose) {
    String portalText = "Test portal: " + name;

    HomePage home = logonToHome("portlettest2", "``````");
    assertTrue(home.portalExists(name), "Portal named '" + name + "' not found.");

    HtmlPortalSection html = new HtmlPortalSection(context, name);
    html.get();
    assertEquals(html.portalText(), portalText);
    assertEquals(html.isCloseable(), canClose);
    assertEquals(html.isMinimisable(), canMinMan);
  }

  // User portlets
  private HomePage dash() {
    return logonToHome("portlettest2", "``````");
  }

  @Test
  public void testBrowsePortal() {
    HomePage home = dash();
    String browseName = context.getFullName("Browse");

    BrowsePortalEditPage portal = new BrowsePortalEditPage(context);
    portal = home.addPortal(portal);

    portal.setTitle(browseName).save(new HomePage(context));
    home = new MenuSection(context).home();

    assertTrue(home.portalExists(browseName));
    BrowsePortalSection browse = new BrowsePortalSection(context, browseName).get();
    assertTrue(browse.topicExists("Browse Activity Plans"));

    browse.delete();
    assertFalse(home.portalExists(browseName));
  }

  @Test
  public void testFavPortal() {
    HomePage home = dash();
    String favName = context.getFullName("Favourite Portal");

    FavPortalEditPage portal = new FavPortalEditPage(context);

    portal = home.addPortal(portal);

    portal.setTitle(favName).save(new HomePage(context));
    home = new MenuSection(context).home();
    assertTrue(home.portalExists(favName));

    String name = "add me";
    String itemName = context.getFullName(name);
    WizardPageTab wizard =
        new ContributePage(context).load().openWizard(GENERIC_TESTING_COLLECTION);

    wizard.editbox(1, itemName);
    SummaryPage summaryTabPage = wizard.save().publish();
    summaryTabPage.addToFavourites().clickAdd();

    // Fav item
    home = new MenuSection(context).home();
    FavPortalSection fav = new FavPortalSection(context, favName).get();
    assertTrue(fav.favouriteExists(itemName));

    fav.clickFavourite(itemName).removeFavourite();
    home = new MenuSection(context).home();
    assertFalse(fav.favouriteExists(itemName));

    // Fav search
    SearchPage search = new SearchPage(context).load();
    search.search(name);
    search.saveSearch(name);

    home = new MenuSection(context).home();
    fav = new FavPortalSection(context, favName).get();
    assertTrue(fav.favouriteSearchExists(name));

    SearchPage favSearchPage = fav.clickFavouriteSearch(name);
    assertTrue(favSearchPage.results().getResult(1).getTitle().equals(itemName));

    FavouriteSearchesPage favSearches =
        new MenuSection(context).clickMenu("Favourites", new FavouritesPage(context)).searches();
    assertTrue(favSearches.results().doesResultExist(name));
    favSearches.delete(name);

    home = new MenuSection(context).home();
    assertFalse(fav.favouriteSearchExists(name));
  }

  @Test
  public void testHtmlPortal() {
    HomePage home = dash();
    String htmlName = context.getFullName("HTML Portal");
    String portalText = "A test portal";
    HtmlPortalEditPage portal = new HtmlPortalEditPage(context);
    portal = home.addPortal(portal);
    portal.setTitle(htmlName);

    portal.setText(portalText);
    portal.save(new HomePage(context));
    home = new MenuSection(context).home();

    assertTrue(home.portalExists(htmlName));
    HtmlPortalSection html = new HtmlPortalSection(context, htmlName).get();

    assertTrue(portalText.equals(html.portalText()));
  }

  @Test
  public void testMyResourcesPortal() {
    HomePage home = dash();
    String myResourceName = context.getFullName("My Resources Portal");

    MyResourcesPortalEditPage portal = new MyResourcesPortalEditPage(context);
    portal = home.addPortal(portal);
    portal.setTitle(myResourceName).save(new HomePage(context));
    home = new MenuSection(context).home();

    assertTrue(home.portalExists(myResourceName));
    MyResourcesPortalSection myResources =
        new MyResourcesPortalSection(context, myResourceName).get();

    myResources.delete();
    assertFalse(home.portalExists(myResourceName));
  }

  @Test
  public void testSearchPortal() {
    HomePage home = dash();
    String searchName = context.getFullName("Search Portal");

    SearchPortalEditPage portal = new SearchPortalEditPage(context);
    portal = home.addPortal(portal);

    portal.setTitle(searchName).save(new HomePage(context));
    home = new MenuSection(context).home();
    assertTrue(home.portalExists(searchName));

    String itemName = context.getFullName("search for me");
    WizardPageTab wizard =
        new ContributePage(context).load().openWizard(GENERIC_TESTING_COLLECTION);

    wizard.editbox(1, itemName);
    wizard.save().publish();

    home = new MenuSection(context).home();
    SearchPortalSection search = new SearchPortalSection(context, searchName).get();
    assertTrue(search.search("search for me").hasResults());
  }

  @Test
  public void testFreemarkerPortal() {
    HomePage home = dash();
    String freemarkerName = context.getFullName("Freemarker Portal");

    FreemarkerPortalEditPage portal = new FreemarkerPortalEditPage(context);
    portal = home.addPortal(portal);

    portal.setTitle(freemarkerName).save(new HomePage(context));
    home = new MenuSection(context).home();
    assertTrue(home.portalExists(freemarkerName));
  }

  @Test
  public void testRecentPortal() {
    HomePage home = dash();
    String recentName = context.getFullName("Recent Contributions Portal");

    // Create Recent Contributions portal
    RecentContributionsEditPage portal = new RecentContributionsEditPage(context);
    portal = home.addPortal(portal);

    portal.setTitle(recentName);
    portal.setStatus("live");
    portal.save(new HomePage(context));
    home = new MenuSection(context).home();

    // Check portal exists
    assertTrue(home.portalExists(recentName));

    // Contribute two items and publish one and save to draft the other
    String liveItemName = context.getFullName("live item");
    WizardPageTab wizard1 =
        new ContributePage(context).load().openWizard(GENERIC_TESTING_COLLECTION);
    wizard1.editbox(1, liveItemName);
    wizard1.save().publish();

    String draftItemName = context.getFullName("draft item");
    WizardPageTab wizard2 =
        new ContributePage(context).load().openWizard("Simple Controls Collection");
    wizard2.editbox(1, draftItemName);
    WebDriverWait waiter = wizard2.save().draft().getWaiter();

    // Check that the live item is displayed.
    assertTrue(checkContributionExistence(waiter, recentName, liveItemName));

    // Edit the portal
    RecentContributionsSection recent = new RecentContributionsSection(context, recentName).get();
    RecentContributionsEditPage edit = recent.edit(portal);
    edit.setStatus("draft");
    edit.checkSelectedCollection();
    edit.save(new HomePage(context));

    // Check that the draft item is displayed
    recent = new RecentContributionsSection(context, recentName).get();
    assertTrue(recent.recentContributionExists(draftItemName));

    String itemToQuery = context.getFullName("query item");
    String description = context.getFullName("query item description");

    WizardPageTab wizard3 =
        new ContributePage(context).load().openWizard(GENERIC_TESTING_COLLECTION);
    wizard3.editbox(1, itemToQuery);
    wizard3.editbox(2, description);
    wizard3.save().publish();

    // Edit portlet for query option
    new MenuSection(context).home();
    recent = new RecentContributionsSection(context, recentName).get();
    edit = recent.edit(portal);
    edit.setQuery("query item");
    edit.setStatus("live");
    edit.checkSelectedCollection();
    waiter = edit.save(new HomePage(context)).getWaiter();

    // Check that the queried item is displayed
    assertTrue(checkContributionExistence(waiter, recentName, itemToQuery));
    recent = new RecentContributionsSection(context, recentName).get();
    assertTrue(recent.descriptionExists(description, true));

    // Edit portlet for description option
    recent = new RecentContributionsSection(context, recentName).get();
    edit = recent.edit(portal);
    edit.setDisplayTitleOnly(true);
    edit.checkSelectedCollection();
    edit.save(new HomePage(context));

    // Check that the description not displayed
    recent = new RecentContributionsSection(context, recentName).get();
    assertFalse(recent.descriptionExists(description, false));
  }

  @Test
  public void testRssPortal() {
    String rssName = context.getFullName("RSS Portal");
    String itemName = context.getFullName("RSS Item");

    HomePage home = dash();

    for (int i = 1; i < 5; i++) {
      WizardPageTab wizard =
          new ContributePage(context).load().openWizard(GENERIC_TESTING_COLLECTION);
      wizard.editbox(1, itemName + " " + i);
      wizard.save().publish();
    }

    WizardPageTab wizard =
        new ContributePage(context).load().openWizard(GENERIC_TESTING_COLLECTION);
    wizard.editbox(1, itemName + " 5");
    wizard.editbox(2, "A description");
    wizard.save().publish();

    SearchPage searchPage = new SearchPage(context).load();
    searchPage.setSort("name");
    searchPage.exactQuery(context.getSubPrefix());
    String rssUrl = searchPage.shareSearch().getRssUrl();

    home = dash();
    RssPortalEditPage portal = new RssPortalEditPage(context);
    home.addPortal(portal);
    portal.setTitle(rssName);
    portal.setUrl(rssUrl);
    portal.setEntries(5);
    portal.save(new HomePage(context));

    RssPortalSection rss = new RssPortalSection(context, rssName).get();
    Assert.assertEquals(rss.countEntries(), 5);
    Assert.assertEquals(rss.getTitle(1), itemName + " 1");
    Assert.assertEquals(rss.getTitle(5), itemName + " 5");
    Assert.assertEquals(rss.getDescription(5), "A description");

    portal = rss.edit(new RssPortalEditPage(context));
    portal.setEntries(2);
    portal.save(new HomePage(context));
    rss = new RssPortalSection(context, rssName).get();
    Assert.assertEquals(rss.countEntries(), 2);
    Assert.assertEquals(rss.getTitle(1), itemName + " 1");
    Assert.assertEquals(rss.getTitle(2), itemName + " 2");
  }

  @Test
  public void testTaskStatisticsPortal() {
    HomePage home = dash();
    String tsName = context.getFullName("Task Statistics Portal");
    TaskStatisticsPortalEditPage tsPortal = new TaskStatisticsPortalEditPage(context);
    tsPortal = home.addPortal(tsPortal);
    tsPortal.setTitle(tsName);
    tsPortal.setTrend("MONTH");

    tsPortal.save(new HomePage(context));
    home = new MenuSection(context).home();

    assertTrue(home.portalExists(tsName));

    TaskStatisticsPortalSection tsSection = new TaskStatisticsPortalSection(context, tsName).get();
    assertTrue(tsSection.isTrendSelected("Monthly"));

    // TODO - Check contents of portlet
  }

  @Override
  protected void cleanupAfterClass() throws Exception {
    logon("portlettest1", "``````");
    String prefix = context.getNamePrefix();
    new DashboardAdminPage(context).load().deleteAll(prefix);
    super.cleanupAfterClass();
  }

  /**
   * Checks if a contribution item exists in the recent contributions section.
   *
   * @param waiter The WebDriverWait object to wait for the condition.
   * @param sectionTitle The title of the recent contributions section.
   * @param itemName The name of the contribution item.
   * @return true if the contribution item exists, false otherwise.
   */
  private boolean checkContributionExistence(
      WebDriverWait waiter, String sectionTitle, String itemName) {
    try {
      waiter.until(
          driver -> {
            new MenuSection(context).home();
            RecentContributionsSection recent =
                new RecentContributionsSection(context, sectionTitle).get();
            return recent.recentContributionExists(itemName);
          });
      return true;
    } catch (TimeoutException e) {
      return false;
    }
  }
}
