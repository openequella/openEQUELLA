package com.tle.webtests.test.workflow.assignment;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.tasklist.TaskListPage;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.pageobject.wizard.controls.SelectUserControl;
import com.tle.webtests.test.AbstractCleanupTest;
import org.testng.annotations.Test;

@TestInstitution("workflow")
public class AutoAssignSimpleTest extends AbstractCleanupTest {
  public AutoAssignSimpleTest() {
    setDeleteCredentials("AutoAssignContributor", "``````");
  }

  @Test
  public void testAutoAssign() {
    logon("AutoAssignContributor", "``````");

    WizardPageTab wizard =
        new ContributePage(context).load().openWizard("Auto Assign By Metadata Collection");
    String itemFullName = context.getFullName("item");
    wizard.editbox(1, itemFullName);
    SelectUserControl selectUser = wizard.selectUser(3);
    selectUser.queryAndSelect("autoassigntarget", "AutoAssignTarget");
    wizard.save().submit();
    logout();

    logon("AutoAssignTarget", "``````");
    TaskListPage taskList = new TaskListPage(context).load();
    taskList.exactQuery(itemFullName).moderate(itemFullName).accept();
  }
}
