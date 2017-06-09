package com.tle.webtests.test.workflow;

import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.portal.TopbarMenuSection;
import com.tle.webtests.pageobject.tasklist.NotificationsPage;
import com.tle.webtests.pageobject.tasklist.TaskListPage;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.test.AbstractCleanupTest;

@TestInstitution("workflow")
public class TasksNotificationsTopbarTest extends AbstractCleanupTest
{
	@Test
	public void sidebarTest()
	{
		final int REPS = 2;
		String itemFullName = context.getFullName("item");
		logon("SidebarUser", "``````");

		assertTrue(checkTasks());
		assertTrue(checkNotifications());

		for( int i = 0; i < REPS; i++ )
		{
			WizardPageTab wizard = new ContributePage(context).load().openWizard("Sidebar Collection");
			wizard.editbox(1, itemFullName);
			wizard.save().submit();

			assertTrue(checkTasks());
			assertTrue(checkNotifications());
		}

		for( int i = 0; i < REPS; i++ )
		{
			TaskListPage taskList = new TaskListPage(context).load();
			taskList.exactQuery(itemFullName).moderate(itemFullName).reject().rejectWithMessage("Reject", null);
			taskList.search("");

			assertTrue(checkTasks());
			assertTrue(checkNotifications());
		}

		for( int i = 0; i < REPS; i++ )
		{
			NotificationsPage np = new NotificationsPage(context).load();
			SummaryPage item = np.exactQuery(itemFullName).getResult(1).viewSummary();
			item.delete();
			item.purge();
			np = new NotificationsPage(context).load();
			np.search("");

			assertTrue(checkTasks());
			assertTrue(checkNotifications());
		}
	}

	private boolean checkNotifications()
	{
		TopbarMenuSection tbs = new TopbarMenuSection(context).get();
		NotificationsPage np = new NotificationsPage(context).load();
		return (tbs.getNumberOfNotifications() == np.getNumberOfResults());
	}

	private boolean checkTasks()
	{
		TopbarMenuSection tbs = new TopbarMenuSection(context).get();
		TaskListPage taskList = new TaskListPage(context).load();
		return (tbs.getNumberOfTasks() == taskList.getNumberOfResults());
	}
}
