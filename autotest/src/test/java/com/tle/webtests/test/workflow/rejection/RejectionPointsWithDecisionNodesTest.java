package com.tle.webtests.test.workflow.rejection;

import static org.testng.Assert.assertEquals;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.searching.ModerateListSearchResults;
import com.tle.webtests.pageobject.tasklist.ModerationView;
import com.tle.webtests.pageobject.tasklist.TaskListPage;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.test.AbstractCleanupTest;
import org.testng.annotations.Test;

@TestInstitution("workflow")
public class RejectionPointsWithDecisionNodesTest extends AbstractCleanupTest {

  public RejectionPointsWithDecisionNodesTest() {
    super("RPWDNT");
    setDeleteCredentials("admin", "``````");
  }

  @Test
  public void testRejectionDecisionNodes() {

    // Logon as admin user and contribute an item to the collection
    logon("admin", "``````");
    WizardPageTab wizard =
        new ContributePage(context)
            .load()
            .openWizard("Sibling Reject Points in Decision Nodes Collection");
    String itemFullName = context.getFullName("item");
    wizard.editbox(1, itemFullName);
    wizard.setCheck(3, "yes", true);
    wizard.setCheck(4, "yes", true);
    wizard.save().submit();
    logout();

    // Login as the moderator and accept the item past the first moderation
    // step.
    logon("SimpleModerator", "``````");
    TaskListPage taskListPage = new TaskListPage(context).load();
    ModerateListSearchResults modResults = taskListPage.exactQuery(itemFullName);
    assertEquals(modResults.getStepName(itemFullName), "Moderation Step 1");
    modResults.moderate(itemFullName).accept();
    logout();

    // login as the second moderator and reject the item back to the first
    // moderation step.
    logon("SecondStepModerator", "``````");
    taskListPage = new TaskListPage(context).load();
    modResults = taskListPage.exactQuery(itemFullName);
    assertEquals(modResults.getStepName(itemFullName), "Moderation Step 2");
    ModerationView tasksTab = modResults.moderate(itemFullName);
    // Click the Assign to me link before rejecting, as previously this was
    // erroring. See #2973
    tasksTab.assignToMe();
    tasksTab.reject().rejectWithMessage("reject", "Moderation Step 1");

    // Check that the item has gone back to the first moderation step.
    logout();
    logon("SimpleModerator", "``````");
    taskListPage = new TaskListPage(context).load();
    modResults = taskListPage.exactQuery(itemFullName);
    assertEquals(modResults.getStepName(itemFullName), "Moderation Step 1");
  }
}
