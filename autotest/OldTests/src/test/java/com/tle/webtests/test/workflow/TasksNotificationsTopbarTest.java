package com.tle.webtests.test.workflow;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.portal.TopbarMenuSection;
import com.tle.webtests.pageobject.tasklist.NotificationsPage;
import com.tle.webtests.pageobject.tasklist.TaskListPage;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.test.AbstractCleanupTest;
import org.testng.Assert;
import org.testng.annotations.Test;

@TestInstitution("workflow")
public class TasksNotificationsTopbarTest extends AbstractCleanupTest {
  @Test
  public void sidebarTest() {
    final int REPS = 2;
    String itemFullName = context.getFullName("item");
    logon("SidebarUser", "``````");

    checkTasks();
    checkNotifications();

    for (int i = 0; i < REPS; i++) {
      WizardPageTab wizard = new ContributePage(context).load().openWizard("Sidebar Collection");
      wizard.editbox(1, itemFullName);
      wizard.save().submit();

      checkTasks();
      checkNotifications();
    }

    for (int i = 0; i < REPS; i++) {
      TaskListPage taskList = new TaskListPage(context).load();
      taskList
          .exactQuery(itemFullName)
          .moderate(itemFullName)
          .reject()
          .rejectWithMessage("Reject", null);
      taskList.search("");

      checkTasks();
      checkNotifications();
    }

    for (int i = 0; i < REPS; i++) {
      NotificationsPage np = new NotificationsPage(context).load();
      SummaryPage item = np.exactQuery(itemFullName).getResult(1).viewSummary();
      item.delete();
      item.purge();
      np = new NotificationsPage(context).load();
      np.search("");

      checkTasks();
      checkNotifications();
    }
  }

  private void checkNotifications() {
    NotificationsPage np = new NotificationsPage(context).load();
    TopbarMenuSection tbs = new TopbarMenuSection(context).get();
    Assert.assertEquals(tbs.getNumberOfNotifications(), np.getNumberOfResults());
  }

  private void checkTasks() {
    TaskListPage taskList = new TaskListPage(context).load();
    TopbarMenuSection tbs = new TopbarMenuSection(context).get();
    Assert.assertEquals(tbs.getNumberOfTasks(), taskList.getNumberOfResults());
  }
}
