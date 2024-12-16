package com.tle.webtests.test.searching.hierarchy;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.hierarchy.TopicListPage;
import com.tle.webtests.pageobject.hierarchy.TopicPage;
import com.tle.webtests.pageobject.portal.MenuSection;
import com.tle.webtests.pageobject.searching.FavouriteSearchList;
import com.tle.webtests.pageobject.searching.FavouritesPage;
import com.tle.webtests.pageobject.searching.SearchPage;
import com.tle.webtests.pageobject.selection.SelectionCheckoutPage;
import com.tle.webtests.pageobject.selection.SelectionSession;
import com.tle.webtests.pageobject.viewitem.AdminTabPage;
import com.tle.webtests.pageobject.viewitem.ModifyKeyResourcePage;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.pageobject.wizard.controls.UniversalControl;
import com.tle.webtests.pageobject.wizard.controls.universal.ResourceUniversalControlType;
import com.tle.webtests.test.AbstractCleanupTest;
import org.testng.Assert;
import org.testng.annotations.Test;
import testng.annotation.OldUIOnly;

/** Legacy tests for hierarchy topic. Old UI only. */
@TestInstitution("fiveo")
public class HierarchyTopicTest extends AbstractCleanupTest {
  @Test(enabled = false, dependsOnMethods = "childInheritance")
  @OldUIOnly
  public void addKeyResource() {
    logon("AutoTest", "automated");
    String topic = "A Topic";
    String itemName = context.getFullName("A Key Resource");

    WizardPageTab wiz = new ContributePage(context).load().openWizard("SOAP and Harvesting");
    wiz.editbox(1, itemName);
    AdminTabPage item = wiz.save().publish().adminTab();

    ModifyKeyResourcePage add = item.modifyKeyResource();
    add.addToHierarchy(topic);

    TopicPage browseAll = new TopicPage(context).load();
    TopicPage topicPage = browseAll.clickSubTopic(topic);
    TopicListPage results = topicPage.results();
    results.doesResultExist(itemName, 1);
    topicPage.setSort("name");
  }

  @Test(dependsOnMethods = "childInheritance")
  @OldUIOnly
  public void lotsOfKeyResources() {
    logon("AutoTest", "automated");

    String topic = "A Topic";
    String itemName = context.getFullName("Lots of Key Resources");

    for (int i = 0; i < 11; i++) {
      WizardPageTab wiz = new ContributePage(context).load().openWizard("SOAP and Harvesting");
      wiz.editbox(1, itemName + " " + i);
      AdminTabPage item = wiz.save().publish().adminTab();

      ModifyKeyResourcePage add = item.modifyKeyResource();
      add.addToHierarchy(topic);
    }

    TopicPage topicPage = new TopicPage(context).load();
    topicPage = topicPage.clickSubTopic(topic);
    topicPage.setSort("name");

    // Key resources are not sorted at the moment (this could change I
    // guess...), so up the number of results per page so that we definitely
    // see all the key resources.
    topicPage.setPerPage("middle");

    TopicListPage results = topicPage.results();
    Assert.assertTrue(results.isResultsAvailable());
    Assert.assertTrue(results.doesResultExist(itemName + " 10", 1));
  }

  @Test
  @OldUIOnly
  public void childInheritance() {
    logon("AutoTest", "automated");
    String topic = "A Topic";

    String itemOne = "SearchFilters - Basic Item";
    String itemTwo = "SearchSettings - Random Item";

    TopicPage topicPage = new MenuSection(context).get().clickTopic(topic);

    topicPage.setSort("name");
    TopicListPage results = topicPage.results();

    Assert.assertTrue(results.doesResultExist(itemOne));
    Assert.assertTrue(results.doesResultExist(itemTwo));
    Assert.assertEquals(topicPage.topicCount("Child"), 1);

    topicPage = topicPage.clickSubTopic("Child");

    Assert.assertFalse(topicPage.resultsHidden());
    Assert.assertTrue(results.doesResultExist(itemTwo));
    Assert.assertFalse(results.doesResultExist(itemOne));
  }

  @Test
  @OldUIOnly
  public void powerSearch() {
    logon("AutoTest", "automated");
    String topic = "Power Search";
    String powerSearch = "A Power Search";

    TopicPage topicPage = new MenuSection(context).get().clickTopic(topic);
    Assert.assertTrue(topicPage.hasPowerSearch());

    SearchPage search = topicPage.clickPowerSearch();
    Assert.assertEquals(search.getSelectedWithin(), powerSearch);
  }

  @Test
  @OldUIOnly
  public void saveAsFavourite() {
    logon("AutoTest", "automated");
    String topic = "A Topic";

    MenuSection menuSection = new MenuSection(context).get();
    TopicPage topicPage = menuSection.clickTopic(topic);

    String searchName = context.getFullName(topic) + " saved";
    topicPage.saveSearch(searchName, topicPage);

    FavouritesPage favouritesPage =
        menuSection.clickMenu("Favourites", new FavouritesPage(context));
    FavouriteSearchList searches = favouritesPage.searches().results();
    searches.doesResultExist(searchName, 1);
    searches.getResultForTitle(searchName, 1).clickTitle();

    Assert.assertTrue(new TopicPage(context, topic).get().topicExists("Child"));
    searches = new FavouritesPage(context).load().searches().results();
    searches.getResultForTitle(searchName, 1).clickActionConfirm("Remove", true, searches);
  }

  @Test
  @OldUIOnly
  public void noResults() {
    logon("AutoTest", "automated");
    String topic = "No Results";

    TopicPage browseAll = new TopicPage(context).load();
    Assert.assertEquals(browseAll.topicCount(topic), 0);
    TopicPage subTopic = browseAll.clickSubTopic(topic);
    Assert.assertFalse(subTopic.resultsHidden());
    Assert.assertEquals(subTopic.results().getResults().size(), 0);
  }

  @Test
  @OldUIOnly
  public void sectionNames() {
    logon("AutoTest", "automated");
    String topic = "Some Children Hidden";

    TopicPage topicPage = new TopicPage(context).load().clickSubTopic(topic);

    Assert.assertEquals(topicPage.getSubtopicSectionName(), "A name");
  }

  @Test
  @OldUIOnly
  public void hiddenResults() {
    logon("AutoTest", "automated");
    String topic = "Results not shown";

    TopicPage browseAll = new TopicPage(context).load();
    Assert.assertEquals(browseAll.topicCount(topic), -1);
    TopicPage subTopic = browseAll.clickSubTopic(topic);
    Assert.assertTrue(subTopic.resultsHidden());
  }

  @Test
  @OldUIOnly
  public void hideNoResultChildren() {
    logon("AutoTest", "automated");
    String topic = "Some Children Hidden";

    TopicPage browseAll = new TopicPage(context).load();
    Assert.assertTrue(browseAll.topicCount(topic) > 0);

    TopicPage topicPage = browseAll.clickSubTopic(topic);

    Assert.assertFalse(topicPage.topicExists("Hidden"));

    WizardPageTab wiz = new ContributePage(context).load().openWizard("SOAP and Harvesting");

    wiz.editbox(1, context.getFullName("SuperSecretWord"));
    wiz.save().publish();

    topicPage = new TopicPage(context).loadBrowsePage().clickSubTopic(topic);
    Assert.assertTrue(topicPage.topicExists("Hidden"));
    topicPage = topicPage.clickSubTopic("Hidden");
    wiz = topicPage.results().getResult(1).viewSummary().adminTab().edit();
    wiz.editbox(1, context.getFullName("NoMore"));
    wiz.saveNoConfirm();
    topicPage = new TopicPage(context).loadBrowsePage().clickSubTopic(topic);
    Assert.assertFalse(topicPage.topicExists("Hidden"));
  }

  @Test
  @OldUIOnly
  public void accessTest() {
    logon("NoSearchCreateUser", "``````");
    TopicPage topicPage = new TopicPage(context).load();
    Assert.assertTrue(topicPage.topicExists("Privilege Test"));
    Assert.assertTrue(topicPage.topicCount("Privilege Test") > 0);

    logon("AutoTest", "automated");
    topicPage = new TopicPage(context).load();
    Assert.assertFalse(topicPage.topicExists("Privilege Test"));
  }

  @Test
  @OldUIOnly
  public void browseHierarchyBreadcrumbTest() {
    logon("AutoTest", "automated");

    WizardPageTab wizard =
        new ContributePage(context).load().openWizard("Navigation and Attachments");
    UniversalControl universalControl = wizard.universalControl(8);
    ResourceUniversalControlType resourceDialog =
        universalControl.addDefaultResource(new ResourceUniversalControlType(universalControl));
    SelectionSession selectionSession = resourceDialog.getSelectionSession();
    TopicPage topicPage = selectionSession.clickBrowseTopLinkMenu(new TopicPage(context));
    topicPage = topicPage.clickSubTopic("A Topic");
    topicPage = topicPage.clickSubTopic("Child");
    Assert.assertTrue(selectionSession.hasBreadcrumbShow());
    selectionSession.finish();
    selectionSession.clickBrowseTopLinkMenu(topicPage);
    Assert.assertTrue(selectionSession.hasBreadcrumbShow());
    SelectionCheckoutPage finish = selectionSession.finish();
    wizard = finish.cancelSelection(wizard);
    // FIXME: wait for dialog disappearance... somehow
    wizard.cancel(new ContributePage(context));
  }
}
