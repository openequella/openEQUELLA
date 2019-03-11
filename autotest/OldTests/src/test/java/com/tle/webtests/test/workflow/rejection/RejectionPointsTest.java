package com.tle.webtests.test.workflow.rejection;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.searching.ModerateListSearchResults;
import com.tle.webtests.pageobject.tasklist.NotificationSearchResults;
import com.tle.webtests.pageobject.tasklist.NotificationsPage;
import com.tle.webtests.pageobject.tasklist.TaskListPage;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.test.AbstractCleanupTest;
import org.testng.annotations.Test;

@TestInstitution("workflow")
public class RejectionPointsTest extends AbstractCleanupTest {

  public RejectionPointsTest() {
    super();
    setDeleteCredentials("admin", "``````");
  }

  @Test
  public void testRejectionPoints() {

    // Logon as the admin user and contribute an item to a workflow
    // collection.
    logon("admin", "``````");
    WizardPageTab wizard =
        new ContributePage(context).load().openWizard("Simple 2 Step with Rejection Points");
    String itemFullName = context.getFullName("item");
    wizard.editbox(1, itemFullName);
    wizard.editbox(2, "We are rejecting back to points");
    wizard.save().submit();
    logout();

    // Log in as the moderator user and moderate the item past the first
    // step.
    logon("SimpleModerator", "``````");
    TaskListPage taskListPage = new TaskListPage(context).load();
    taskListPage.exactQuery(itemFullName).moderate(itemFullName).accept();
    logout();

    // Logon as the SecondStepModerator user and reject the item back to the
    // first step
    logon("SecondStepModerator", "``````");
    taskListPage = new TaskListPage(context).load();
    ModerateListSearchResults modResults = taskListPage.exactQuery(itemFullName);
    assertEquals(modResults.getStepName(itemFullName), "Second Step");
    modResults.moderate(itemFullName).reject().rejectWithMessage("reject", "First Step");
    logout();

    // Log back in as the SimpleModerator and moderate the item past the
    // first step again.
    logon("SimpleModerator", "``````");
    taskListPage = new TaskListPage(context).load();
    modResults = taskListPage.exactQuery(itemFullName);
    modResults.moderate(itemFullName).accept();
    logout();

    // Log in as the SecondStepModerator and reject the item back to a
    // serial node.
    logon("SecondStepModerator", "``````");
    taskListPage = new TaskListPage(context).load();
    modResults = taskListPage.exactQuery(itemFullName);
    assertEquals(modResults.getStepName(itemFullName), "Second Step");
    modResults.moderate(itemFullName).reject().rejectWithMessage("reject again", "Serial Node 1");
    logout();

    // Log back in as the SimpleModerator and moderate the item past the
    // first step again.
    logon("SimpleModerator", "``````");
    taskListPage = new TaskListPage(context).load();
    modResults = taskListPage.exactQuery(itemFullName);
    modResults.moderate(itemFullName).accept();
    logout();

    // Log in as the SecondStepModerator and reject the item back to the
    // original contributor
    logon("SecondStepModerator", "``````");
    taskListPage = new TaskListPage(context).load();
    modResults = taskListPage.exactQuery(itemFullName);
    assertEquals(modResults.getStepName(itemFullName), "Second Step");
    modResults
        .moderate(itemFullName)
        .reject()
        .rejectWithMessage("reject again", "Original Contributor");
    logout();

    // Log in as the original contributor and verify that the item is
    // rejected.
    logon("admin", "``````");
    NotificationsPage notePage = new NotificationsPage(context).load();
    NotificationSearchResults noteResults = notePage.exactQuery(itemFullName);
    assertTrue(noteResults.isReasonRejected(itemFullName));
    logout();
  }
}
