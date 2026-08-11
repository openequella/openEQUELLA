package com.tle.webtests.failsome;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import com.tle.webtests.framework.Name;
import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.HomePage;
import com.tle.webtests.pageobject.PrefixedName;
import com.tle.webtests.pageobject.myresources.MyResourcesPage;
import com.tle.webtests.pageobject.portal.TasksPortalEditPage;
import com.tle.webtests.pageobject.portal.TasksPortalSection;
import com.tle.webtests.pageobject.searching.ItemSearchResult;
import com.tle.webtests.pageobject.searching.SearchPage;
import com.tle.webtests.pageobject.settings.ScheduledTasksPage;
import com.tle.webtests.pageobject.tasklist.ModerationView;
import com.tle.webtests.pageobject.tasklist.NotificationsPage;
import com.tle.webtests.pageobject.tasklist.NotificationsPage.SelectedDialog;
import com.tle.webtests.pageobject.tasklist.TaskListPage;
import com.tle.webtests.pageobject.viewitem.ShareWithOthersPage;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.test.AbstractCleanupTest;
import java.util.Calendar;
import java.util.Date;
import org.testng.annotations.Test;

@TestInstitution("workflow")
public class NotificationsTest extends AbstractCleanupTest {
  @Name("Bad Url")
  private static PrefixedName BAD_URL;

  @Name("Rejected Item")
  private static PrefixedName REJECTED_ITEM;

  @Name("Other User Notifying You")
  private static PrefixedName OTHER_USER;

  @Name("Watched Collection")
  private static PrefixedName WATCHED_COLLECTION;

  @Name("Moderation Overdue")
  private static PrefixedName MOD_OVERDUE;

  @Name("Moderation Overdue So Reject")
  private static PrefixedName MOD_OVERDUE_REJECT;

  @Name("Moderation Overdue So Accept")
  private static PrefixedName MOD_AUTO_ACCEPT;

  @Name("Bulk - ")
  private static PrefixedName BULK_ITEM_PFX;

  @Test
  public void setupNotifications() {
    // Rejected
    logon("simplemoderator", "``````");
    WizardPageTab wizard = new ContributePage(context).load().openWizard("Simple 1 Step");
    wizard.editbox(1, REJECTED_ITEM);
    wizard.save().submit();
    TaskListPage taskList = new TaskListPage(context).load();
    ModerationView mv = taskList.exactQuery(REJECTED_ITEM).moderate(REJECTED_ITEM);
    mv.reject().rejectWithMessage("Boo", null);

    // Bad URL
    wizard = new ContributePage(context).load().openWizard("Notification Collection");
    wizard.editbox(1, BAD_URL);
    wizard.addUrl(4, "http://badURL", "badURL");
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.MONTH, 3);
    wizard.calendar(3).clearDate();
    wizard.calendar(3).setDate(cal);
    wizard.save().submit();

    // Moderation Overdue
    wizard = new ContributePage(context).load().openWizard("Notification Collection");
    wizard.editbox(1, MOD_OVERDUE);
    wizard.save().submit();
    logout();

    logon("admin", "``````");
    // Moderation Overdue, automatic rejection
    wizard =
        new ContributePage(context)
            .load()
            .openWizard("Different Collection with moderation deadline");
    wizard.editbox(1, MOD_OVERDUE_REJECT);
    wizard.editbox(
        2,
        "Given the existence as uttered forth in the public works of Puncher and Wattman of a"
            + " personal god quaquaquaqua with white beard quaquaquaqua outside time without"
            + " extension who from the heights of divine apathia divine athambia divine aphasisa"
            + " loves us dearly with some exceptions for reasons unknown but time will tell ...");
    Calendar sixMonthsAgo = Calendar.getInstance();
    sixMonthsAgo.add(Calendar.MONTH, -6);
    Date syxmunsago = sixMonthsAgo.getTime();
    wizard.calendar(3).setDate(syxmunsago);
    wizard.save().submit();
    logout();

    // Owner notifying you
    logon("TLE_ADMINISTRATOR", testConfig.getAdminPassword());
    wizard = new ContributePage(context).load().openWizard("No Workflow");
    wizard.editbox(1, OTHER_USER);
    wizard.save().draft();
    SearchPage sp = new SearchPage(context).load();
    sp.setIncludeNonLive(true);
    ItemSearchResult itemSearchResult = sp.search(namePrefix).getResultForTitle(OTHER_USER, 1);
    SummaryPage summaryPage = itemSearchResult.viewSummary();

    ShareWithOthersPage swop = summaryPage.share();
    swop.selectUser("moderator", "SimpleModerator", "Simple Moderator");
    summaryPage.edit().save().publish();

    // Watched going live
    wizard = new ContributePage(context).load().openWizard("No Workflow");
    wizard.editbox(1, WATCHED_COLLECTION);
    wizard.save().publish();

    runNotifyNewTasksAsSystemSuperUserAndReloginAs("simplemoderator", "``````");
    // Do notifications exist??
    NotificationsPage np = new NotificationsPage(context).load();

    // filtered to make sure the right types of notifications made
    np.setReasonFilter("overdue");
    np.search(namePrefix);
    assertTrue(np.results().doesResultExist(MOD_OVERDUE));

    // FIXME: Bad URL notification proving difficult to engineer
    // np.setReasonFilter("badurl");
    // assertTrue(np.results().doesResultExist(BAD_URL));

    np.setReasonFilter("rejected");
    assertTrue(np.results().doesResultExist(REJECTED_ITEM));
    np.setReasonFilter("wentlive");
    assertTrue(np.results().doesResultExist(OTHER_USER));
    np.setReasonFilter("wentliv2");
    assertTrue(np.results().doesResultExist(WATCHED_COLLECTION));
    logout();

    // logon as admin to verify that the contribution to was automatically
    // rejected
    logon("admin", "``````");
    np = new NotificationsPage(context).load();
    np.setReasonFilter("rejected");
    assertTrue(np.results().doesResultExist(MOD_OVERDUE_REJECT));
    logout();
  }

  @Test
  public void autoAcceptanceTest() {
    createItemAndModeratePastFirstStep("admin", "``````");

    runNotifyNewTasksAsSystemSuperUserAndReloginAs("SecondStepModerator", "``````");

    // Not the subject of a notification because it was approved
    // automatically
    NotificationsPage np = new NotificationsPage(context).load();
    np.search(namePrefix);
    assertFalse(np.hasResults());
    SearchPage sp = new SearchPage(context).load();
    sp.setIncludeNonLive(true);
    ItemSearchResult itemSearchResult =
        sp.search(MOD_AUTO_ACCEPT).getResultForTitle(MOD_AUTO_ACCEPT, 1);

    // not findable amongst the notifications ...
    assertNotNull(itemSearchResult);

    // ... but it is findable amongst the live items
    assertTrue("live".equalsIgnoreCase(itemSearchResult.getStatus()));

    logout();
  }

  @Test(dependsOnMethods = "setupNotifications")
  public void taskPortalTest() {
    logon("simplemoderator", "``````");
    HomePage dashboard = new HomePage(context).load();
    String tsName = context.getFullName("Tasks Portal");
    TasksPortalEditPage tPortal = new TasksPortalEditPage(context);
    tPortal = dashboard.addPortal(tPortal);
    dashboard = tPortal.setTitle(tsName).save(new HomePage(context));
    TasksPortalSection tsSection = new TasksPortalSection(context, tsName).get();
    int numNotifications = tsSection.getNumberNotifications();
    assertTrue(numNotifications >= 5);
    // Owner Notified
    NotificationsPage np = tsSection.ownerNotified();
    assertTrue(np.ensureReasonFilterSelected("wentlive"));
    assertTrue(np.hasResults());
    dashboard = new HomePage(context).load();
    // Rejected
    np = tsSection.rejected();
    assertTrue(np.ensureReasonFilterSelected("rejected"));
    assertTrue(np.hasResults());
    dashboard = new HomePage(context).load();
    // Bad url
    // FIXME: Bad URL notification proving difficult to engineer
    // np = tsSection.badURLs();
    // assertTrue(np.ensureReasonFilterSelected("badurl"));
    // assertTrue(np.hasResults());
    dashboard = new HomePage(context).load();
    // Watched Collection
    np = tsSection.watchedLive();
    assertTrue(np.ensureReasonFilterSelected("wentliv2"));
    assertTrue(np.hasResults());
    dashboard = new HomePage(context).load();
    // Overdue
    np = tsSection.overdue();
    assertTrue(np.ensureReasonFilterSelected("overdue"));
    assertTrue(np.hasResults());
    dashboard = new HomePage(context).load();
    new TasksPortalSection(context, tsName).get().delete();
  }

  @Test(dependsOnMethods = "taskPortalTest")
  public void autoDeleteTest() {
    // Deal with Notifications
    logon("simplemoderator", "``````");

    // bad URL - fix url
    SearchPage sp = new SearchPage(context).load();
    sp.setIncludeNonLive(true);
    SummaryPage summaryPage = sp.search(BAD_URL).getResultForTitle(BAD_URL, 1).viewSummary();
    WizardPageTab wizard = summaryPage.edit();
    wizard.universalControl(4).deleteResource("badURL");
    wizard.addUrl(4, "http://www.google.com");
    wizard.saveNoConfirm();
    sp = new SearchPage(context).load();
    summaryPage = sp.search(BAD_URL).getResultForTitle(BAD_URL, 1).viewSummary();
    wizard = summaryPage.adminTab().redraft();
    wizard.save().submit();

    // Rejected - resubmit -> accept
    sp = new SearchPage(context).load();
    summaryPage = sp.search(REJECTED_ITEM).getResultForTitle(REJECTED_ITEM, 1).viewSummary();
    wizard = summaryPage.adminTab().redraft();
    wizard.save().submit();
    TaskListPage taskList = new TaskListPage(context).load();
    ModerationView mv = taskList.exactQuery(REJECTED_ITEM).moderate(REJECTED_ITEM);
    mv.accept();

    // Watched Item - archive
    sp = new SearchPage(context).load();
    summaryPage =
        sp.search(WATCHED_COLLECTION).getResultForTitle(WATCHED_COLLECTION, 1).viewSummary();
    summaryPage.archive();

    // Moderation Overdue - moderate
    taskList = new TaskListPage(context).load();
    mv = taskList.exactQuery(MOD_OVERDUE).moderate(MOD_OVERDUE);
    mv.accept();

    // Owner Notify - archive
    sp = new SearchPage(context).load();
    summaryPage = sp.search(OTHER_USER).getResultForTitle(OTHER_USER, 1).viewSummary();
    summaryPage.archive();

    runNotifyNewTasksAsSystemSuperUserAndReloginAs("simplemoderator", "``````");

    // Are Notifications Gone Automatically??
    NotificationsPage np = new NotificationsPage(context).load();
    np.search(namePrefix);
    assertFalse(np.hasResults());
    logout();

    // delete the auto-rejected item created by admin, and check
    // notification also deleted
    logon("admin", "``````");
    MyResourcesPage myResourcesPage = new MyResourcesPage(context, "all").load();
    summaryPage =
        myResourcesPage
            .exactQuery(MOD_OVERDUE_REJECT)
            .getResultForTitle(MOD_OVERDUE_REJECT)
            .viewSummary();
    summaryPage.delete();
    np = new NotificationsPage(context).load();
    np.search(namePrefix);
    assertFalse(np.hasResults());
    logout();
  }

  private void createItemAndModeratePastFirstStep(String creatingUser, String pwd) {
    logon(creatingUser, pwd);
    // Contribute another item, this one to be manually moderated/approved
    // at first step, and automatically approved/ published as second step
    WizardPageTab wizard =
        new ContributePage(context)
            .load()
            .openWizard("Different Collection with moderation deadline");
    wizard.editbox(1, MOD_AUTO_ACCEPT);
    wizard.editbox(
        2,
        "Given the existence as uttered forth in the public works of Puncher and Wattman of a"
            + " personal god quaquaquaqua with white beard quaquaquaqua outside time without"
            + " extension who from the heights of divine apathia divine athambia divine aphasisa"
            + " loves us dearly with some exceptions for reasons unknown but time will tell ...");
    // first calendar at control[3] defaults to current date (pre-configured
    // in collection definition)
    Calendar sixMonthsAgo = Calendar.getInstance();
    sixMonthsAgo.add(Calendar.MONTH, -6);
    wizard.calendar(4).setDate(sixMonthsAgo);
    wizard.save().submit();

    logout();

    logon("simplemoderator", "``````");
    TaskListPage taskList = new TaskListPage(context).load();
    ModerationView mv = taskList.exactQuery(MOD_AUTO_ACCEPT).moderate(MOD_AUTO_ACCEPT);
    mv.acceptToMessagePage()
        .acceptWithMessage(
            "Moderated manually to first stage, should now be Automatically Accepted at second"
                + " stage");
    runNotifyNewTasksAsSystemSuperUserAndReloginAs(null, null);
  }

  private void runNotifyNewTasksAsSystemSuperUserAndReloginAs(
      String reloginUser, String reloginPasswd) {
    logout();
    logon("TLE_ADMINISTRATOR", testConfig.getAdminPassword());
    ScheduledTasksPage stp = new ScheduledTasksPage(context).load();
    stp.runCheckModeration();
    stp.runNotifyNewTasks();
    stp.runCheckUrls();
    logout();
    if (reloginUser != null) {
      logon(reloginUser, reloginPasswd);
    }
    // We MUST wait for a random amount of time for these tasks to finish.
    // I'm going to wait for 5 seconds, but it may require more.
    try {
      Thread.sleep(5000);
    } catch (InterruptedException e) {
      // Oh well.
    }
  }

  @Test(dependsOnMethods = "autoDeleteTest")
  public void testBulkOperations() {
    logon("simplemoderator", "``````");
    for (int i = 1; i < 6; i++) {
      WizardPageTab wizard = new ContributePage(context).load().openWizard("No Workflow");
      wizard.editbox(1, BULK_ITEM_PFX.toString() + i);
      wizard.save().publish();
    }
    runNotifyNewTasksAsSystemSuperUserAndReloginAs("simplemoderator", "``````");
    NotificationsPage np = new NotificationsPage(context).load();
    np.search(BULK_ITEM_PFX);
    assertEquals("wrong number of notifications", 5, np.getNumberOfResults());
    np.selectNotification(1).selectNotification(2).selectNotification(3);
    assertEquals("wrong number of items selected", 3, np.countSelections());
    SelectedDialog dialog = np.viewSelected();
    dialog.unselectNotification(2);
    np = dialog.closeDialog();
    assertEquals("wrong number of items selected", 2, np.countSelections());
    np.clearSelected();
    assertEquals("wrong number of items items cleared", 3, np.getNumberOfResults());
    assertEquals("selected notifications left over", 0, np.countSelections());
    np.selectAll();
    assertEquals("select all didn't select all!", np.getNumberOfResults(), np.countSelections());
    np.unselectAll();
    assertEquals(0, np.countSelections());
    np.selectAll();
    np.clearSelected();
    np.search(BULK_ITEM_PFX);
    assertFalse(np.hasResults());
    np.clearSelectedNoSelections();
    logout();
  }

  @Override
  protected void cleanupAfterClass() throws Exception {
    super.cleanupAfterClass();
    deleteItemsWithPrefix(getContext(), "simplemoderator", "``````", BULK_ITEM_PFX.toString());
  }
}
