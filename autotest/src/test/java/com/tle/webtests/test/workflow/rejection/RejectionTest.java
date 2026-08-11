package com.tle.webtests.test.workflow.rejection;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.searching.ModerateListSearchResults;
import com.tle.webtests.pageobject.tasklist.NotificationSearchResults;
import com.tle.webtests.pageobject.tasklist.NotificationsPage;
import com.tle.webtests.pageobject.tasklist.TaskListPage;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.ModerationMessagePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.test.AbstractCleanupTest;
import org.testng.annotations.Test;

@TestInstitution("workflow")
public class RejectionTest extends AbstractCleanupTest {

  private static final String ITEM_NAME = "RejectItem";

  public RejectionTest() {
    super("Reject Item");
    setDeleteCredentials("admin", "``````");
  }

  @Test
  public void testRejection() {
    logon("admin", "``````");
    WizardPageTab wizardPage = new ContributePage(context).load().openWizard("Simple 2 Step");
    String itemFullName = context.getFullName(ITEM_NAME);
    wizardPage.editbox(1, itemFullName);
    wizardPage.save().submit();
    logout();

    logon("SimpleModerator", "``````");
    TaskListPage taskListPage = new TaskListPage(context).load();
    ModerateListSearchResults modResults = taskListPage.exactQuery(itemFullName);
    ModerationMessagePage rejectPage = modResults.moderate(itemFullName).reject();
    rejectPage.rejectWithMessage("reject", null);
    logout();

    // Redraft
    logon("admin", "``````");
    NotificationsPage notePage = new NotificationsPage(context).load();
    NotificationSearchResults noteResults = notePage.exactQuery(itemFullName);
    noteResults.viewFromTitle(itemFullName).adminTab().redraft().save().submit();

    // Accept first step
    logon("SimpleModerator", "``````");
    taskListPage = new TaskListPage(context).load();
    modResults = taskListPage.exactQuery(itemFullName);
    modResults.moderate(itemFullName).accept();
    logout();

    // Second step
    logon("SecondStepModerator", "``````");
    taskListPage = new TaskListPage(context).load();
    modResults = taskListPage.exactQuery(itemFullName);
    assertEquals(modResults.getStepName(itemFullName), "Second Step");
    rejectPage = modResults.moderate(itemFullName).reject();
    rejectPage.rejectWithMessage("reject", null);

    logout();
    logon("admin", "``````");
    notePage = new NotificationsPage(context).load();
    noteResults = notePage.exactQuery(itemFullName);
    assertTrue(noteResults.isReasonRejected(itemFullName));
  }
}
