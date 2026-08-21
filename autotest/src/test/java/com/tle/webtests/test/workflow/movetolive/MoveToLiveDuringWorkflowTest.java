package com.tle.webtests.test.workflow.movetolive;

import static org.testng.Assert.assertEquals;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.searching.ModerateListSearchResults;
import com.tle.webtests.pageobject.searching.SearchPage;
import com.tle.webtests.pageobject.tasklist.TaskListPage;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.test.AbstractCleanupTest;
import org.testng.annotations.Test;

@TestInstitution("workflow")
public class MoveToLiveDuringWorkflowTest extends AbstractCleanupTest {

  public MoveToLiveDuringWorkflowTest() {
    super("MTLDWT");
    setDeleteCredentials("admin", "``````");
  }

  @Test
  public void moveToLive() {

    // Login as admin user and contribute an item to the collection
    logon("admin", "``````");

    WizardPageTab wizard =
        new ContributePage(context).load().openWizard("Move to Live During Moderation");
    String itemFullName = context.getFullName("item");
    wizard.editbox(1, itemFullName);
    wizard.save().submit();
    logout();

    // Login as SimpleModerator and moderate the item at the first step.
    logon("SimpleModerator", "``````");
    TaskListPage taskListPage = new TaskListPage(context).load();
    ModerateListSearchResults modResults = taskListPage.exactQuery(itemFullName);
    assertEquals(modResults.getStepName(itemFullName), "moderation step 1");
    modResults.moderate(itemFullName).assignToMe().accept();
    // After moderation, check that the item has gone life after the first
    // moderation step
    SearchPage.searchAndView(context, itemFullName);
    logout();

    // Login as the SecondStepModerator and check that the item is still
    // there to moderate, and is still live once moderated from second step.
    logon("SecondStepModerator", "``````");
    taskListPage = new TaskListPage(context).load();
    modResults = taskListPage.exactQuery(itemFullName);
    modResults.moderate(itemFullName).assignToMe().accept();
    SearchPage.searchAndView(context, itemFullName);
    logout();
  }
}
