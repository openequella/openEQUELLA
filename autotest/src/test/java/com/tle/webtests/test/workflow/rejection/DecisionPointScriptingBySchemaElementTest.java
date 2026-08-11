package com.tle.webtests.test.workflow.rejection;

import static org.testng.Assert.assertEquals;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.searching.ModerateListSearchResults;
import com.tle.webtests.pageobject.searching.SearchPage;
import com.tle.webtests.pageobject.tasklist.ModerationView;
import com.tle.webtests.pageobject.tasklist.TaskListPage;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.test.AbstractCleanupTest;
import org.testng.annotations.Test;

@TestInstitution("workflow")
public class DecisionPointScriptingBySchemaElementTest extends AbstractCleanupTest {

  public DecisionPointScriptingBySchemaElementTest() {
    super("DPSBSET");
    setDeleteCredentials("admin", "``````");
  }

  @Test
  public void scriptingByContributorRole() {

    // Login as the admin user and contribute and item to the collection and
    // submit it to moderation.
    logon("admin", "``````");
    WizardPageTab wizard =
        new ContributePage(context).load().openWizard("Decision Point Scripting by Schema element");
    String itemFullName = context.getFullName("item");
    wizard.editbox(1, itemFullName);
    wizard.setCheck(3, "yes", true);
    wizard.save().submit();
    logout();

    // login as the moderator and accept the item through moderation.
    logon("SimpleModerator", "``````");
    TaskListPage taskListPage = new TaskListPage(context).load();
    ModerateListSearchResults modResults = taskListPage.exactQuery(itemFullName);
    assertEquals(modResults.getStepName(itemFullName), "Moderation Step 1");
    ModerationView tasksTab = modResults.moderate(itemFullName);
    tasksTab.assignToMe();
    tasksTab.accept();
    // check that it was accepted, and the item is now live.
    SearchPage.searchAndView(context, itemFullName);
    logout();
  }
}
