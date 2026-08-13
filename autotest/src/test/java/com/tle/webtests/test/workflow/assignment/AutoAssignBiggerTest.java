package com.tle.webtests.test.workflow.assignment;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.tasklist.TaskListPage;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.pageobject.wizard.controls.SelectUserControl;
import com.tle.webtests.test.AbstractCleanupTest;
import org.testng.annotations.Test;

@TestInstitution("workflow")
public class AutoAssignBiggerTest extends AbstractCleanupTest {
  @Test
  public void testAutoAssign() {
    String mod1 = "SimpleModerator";
    String mod2 = "SecondStepModerator";
    logon("admin", "``````");

    WizardPageTab wizard = new ContributePage(context).load().openWizard("2 Step with auto assign");
    String itemFullName = context.getFullName("item");
    wizard.editbox(1, itemFullName);
    SelectUserControl selectUser = wizard.selectUser(2);
    selectUser.queryAndSelect(mod2, mod2);
    selectUser.queryAndSelect(mod1, mod1);
    wizard.save().submit();
    logout();

    logon(mod2, "``````");
    TaskListPage taskList = new TaskListPage(context).load();
    taskList.exactQuery(itemFullName).moderate(itemFullName);
    logout();

    logon(mod1, "``````");
    taskList = new TaskListPage(context).load();
    wizard = taskList.exactQuery(itemFullName).getResult(1).viewSummary().edit();
    selectUser = wizard.selectUser(2); // Needed? cbf to check
    selectUser.removeUser(mod1);
    wizard.saveNoConfirm();

    taskList = new TaskListPage(context).load();
    assertFalse(taskList.exactQuery(itemFullName).isResultsAvailable());
    logout();

    logon(mod2, "``````");
    taskList = new TaskListPage(context).load();
    wizard = taskList.exactQuery(itemFullName).getResult(1).viewSummary().edit();
    selectUser = wizard.selectUser(2);
    selectUser.queryAndSelect(mod1, mod1);
    wizard.saveNoConfirm();
    logout();

    logon(mod1, "``````");
    taskList = new TaskListPage(context).load();
    wizard = taskList.exactQuery(itemFullName).getResult(1).viewSummary().edit();
    selectUser = wizard.selectUser(2);
    selectUser.removeUser(mod1);
    selectUser.removeUser(mod2);
    wizard.saveNoConfirm();
    logout();

    // Both mods removed, should go to next step
    logon("admin", "``````");
    taskList = new TaskListPage(context).load();
    assertTrue(
        taskList.exactQuery(itemFullName).getResult(1).getStepName().equalsIgnoreCase("Step 2"));
    logout();
  }
}
