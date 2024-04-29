package com.tle.webtests.test.dynamichierarchy;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.HomePage;
import com.tle.webtests.pageobject.hierarchy.TopicListPage;
import com.tle.webtests.pageobject.hierarchy.TopicPage;
import com.tle.webtests.pageobject.portal.MenuSection;
import com.tle.webtests.pageobject.searching.ItemListPage;
import com.tle.webtests.pageobject.searching.ItemSearchResult;
import com.tle.webtests.pageobject.searching.SearchPage;
import com.tle.webtests.pageobject.viewitem.AdminTabPage;
import com.tle.webtests.pageobject.viewitem.ModifyKeyResourcePage;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.test.AbstractCleanupTest;
import org.testng.annotations.Test;

// todo: lots of switching between UI modes were added to get the test passed.
// We will remove these once we work on OEQ-1701 and OEQ-1702
@TestInstitution("hierarchy")
public class DynamicHierarchyTest extends AbstractCleanupTest {
  final String TOPIC_1 = "dynamic_topic 1";
  final String TOPIC_2 = "dynamic_topic 2";
  final String TOPIC_3 = "dynamic_topic 3";
  final String TESTING_ITEM = "Testing item 3";

  @Test
  public void checkGeneratedHierarchyMenu() {
    logon("AutoTest", "automated");
    HomePage homePage = new HomePage(context).load();
    assertTrue(homePage.isTopicTagVisible(TOPIC_1));
    assertTrue(homePage.isTopicTagVisible(TOPIC_2));
    assertTrue(homePage.isTopicTagVisible(TOPIC_3));
  }

  @Test
  public void modifyKeyResourceLinkOnItemSummary() {
    logon("AutoTest", "automated");

    SearchPage searchPage = new SearchPage(context).load();
    ItemListPage itemList = searchPage.search();
    SummaryPage itemSummary = itemList.viewFromTitle(TESTING_ITEM);
    AdminTabPage item = itemSummary.adminTab();

    // MODIFY_KEY_RESOURCE is granted to teacher user
    assertTrue(item.canModifyKeyResource());
    ModifyKeyResourcePage keyResourcePage = item.modifyKeyResource();
    // add key resource to first dynamic hierarchy
    keyResourcePage.addToHierarchy(TOPIC_1);

    if (getTestConfig().isNewUI()) {
      logon("TLE_ADMINISTRATOR", testConfig.getAdminPassword());
      setNewUI(false);
    }
    TopicPage browseAll = new TopicPage(context).load();
    TopicPage topicPage = browseAll.clickSubTopic(TOPIC_1);
    TopicListPage results = topicPage.results();
    assertTrue(results.isResultsAvailable());
    // check if the item is showed as key resource in the first hierarchy topic
    assertTrue(
        results.doesKeyResourceExist(TESTING_ITEM, 1),
        TESTING_ITEM + " doesn't exist in " + TOPIC_1);
    if (getTestConfig().isNewUI()) {
      logon("TLE_ADMINISTRATOR", testConfig.getAdminPassword());
      setNewUI(true);
    }
  }

  @Test(dependsOnMethods = "modifyKeyResourceLinkOnItemSummary")
  public void addToHierarchyLinkOnSearchPage() {
    logon("AutoTest", "automated");

    SearchPage searchPage = new SearchPage(context).load();
    ItemListPage results = searchPage.exactQuery(TESTING_ITEM);

    // link to modify key resource page
    ItemSearchResult resultForTitle = results.getResultForTitle(TESTING_ITEM, 1);
    ModifyKeyResourcePage keyResourcePage = resultForTitle.addToHierarchy();
    // remove key resource from the first hierarchy
    keyResourcePage.removeKeyResourceFromHierarchy(TOPIC_1);
    keyResourcePage.addToHierarchy(TOPIC_2);

    if (getTestConfig().isNewUI()) {
      logon("TLE_ADMINISTRATOR", testConfig.getAdminPassword());
      setNewUI(false);
    }
    TopicPage browseAll1 = new TopicPage(context).load();
    TopicPage topicPage1 = browseAll1.clickSubTopic(TOPIC_1);
    TopicListPage itemList1 = topicPage1.results();
    // the item should not show as key resource in the dynamic hierarchy
    assertFalse(itemList1.doesKeyResourceExist(TESTING_ITEM, 1));

    // only way to lose context is by logout or breadcrumb
    TopicPage browseAll2 = topicPage1.clickBrowseBreadcrumb();
    TopicPage topicPage2 = browseAll2.clickSubTopic(TOPIC_2);
    TopicListPage itemList2 = topicPage2.results();
    assertTrue(itemList2.doesKeyResourceExist(TESTING_ITEM, 1));
    if (getTestConfig().isNewUI()) {
      logon("TLE_ADMINISTRATOR", testConfig.getAdminPassword());
      setNewUI(true);
    }
  }

  @Test(dependsOnMethods = "addToHierarchyLinkOnSearchPage")
  public void topicPageLinksAndCountNumberCheck() {
    logon("AutoTest", "automated");
    String itemName = "Testing item 1";

    if (getTestConfig().isNewUI()) {
      logon("TLE_ADMINISTRATOR", testConfig.getAdminPassword());
      setNewUI(false);
    }
    TopicPage browseAll = new TopicPage(context).load();
    TopicPage topicPage = browseAll.clickSubTopic(TOPIC_1);
    TopicListPage results = topicPage.results();
    results.getResultForTitle(itemName, 1).addAsKeyResource();
    int resultCount = results.getTotalAvailable();
    assertTrue(results.doesKeyResourceExist(itemName, 1));

    TopicPage topicListPage = topicPage.clickBrowseBreadcrumb();
    int itemCount = topicListPage.topicCount(TOPIC_1);
    assertEquals(Integer.toString(resultCount), Integer.toString(itemCount));

    TopicPage backToTopic = topicListPage.clickSubTopic(TOPIC_1);
    TopicListPage sameResult = backToTopic.results();
    results.getResultForTitle(itemName, 1).removeKeyResource();
    int resultRecord = sameResult.getTotalAvailable();
    assertFalse(sameResult.doesKeyResourceExist(itemName, 1));

    TopicPage browseAgain = topicPage.clickBrowseBreadcrumb();
    int countAgain = browseAgain.topicCount(TOPIC_1);
    assertEquals(Integer.toString(resultRecord), Integer.toString(countAgain));
    if (getTestConfig().isNewUI()) {
      logon("TLE_ADMINISTRATOR", testConfig.getAdminPassword());
      setNewUI(true);
    }
  }

  @Test
  // key resource references to an item should be removed when item is deleted
  public void testKeyResourceReferenceRemove() {
    logon("AutoTest", "automated");

    String itemName = "Testing item 4";
    String topicName = "topic 4";

    WizardPageTab wiz =
        new MenuSection(context).clickMenu("Contribute", new WizardPageTab(context, 0));
    wiz.editbox(1, itemName);
    wiz.editbox(2, topicName);
    AdminTabPage item = wiz.save().publish().adminTab();

    ModifyKeyResourcePage page = item.modifyKeyResource();
    page.addToHierarchy(TOPIC_1);
    item.delete();
    item.purge();

    if (getTestConfig().isNewUI()) {
      logon("TLE_ADMINISTRATOR", testConfig.getAdminPassword());
      setNewUI(false);
    }
    TopicPage browseAll = new TopicPage(context).load();
    TopicPage topicPage = browseAll.clickSubTopic(TOPIC_1);
    TopicListPage results = topicPage.results();
    assertFalse(results.doesKeyResourceExist(itemName, 1));
    if (getTestConfig().isNewUI()) {
      logon("TLE_ADMINISTRATOR", testConfig.getAdminPassword());
      setNewUI(true);
    }
  }
}
