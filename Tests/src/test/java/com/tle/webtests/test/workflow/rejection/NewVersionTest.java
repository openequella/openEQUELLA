package com.tle.webtests.test.workflow.rejection;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.searching.ItemAdminPage;
import com.tle.webtests.pageobject.searching.ItemListPage;
import com.tle.webtests.pageobject.searching.ModerateListSearchResults;
import com.tle.webtests.pageobject.searching.SearchPage;
import com.tle.webtests.pageobject.tasklist.ModerationView;
import com.tle.webtests.pageobject.tasklist.TaskListPage;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.test.AbstractCleanupTest;

@TestInstitution("workflow")
public class NewVersionTest extends AbstractCleanupTest
{

	public NewVersionTest()
	{
		setDeleteCredentials("admin", "``````");
	}

	@Test
	public void newVersion()
	{

		// Login as admin user and contribute an item to the collection
		logon("admin", "``````");

		WizardPageTab wizard = new ContributePage(context).load().openWizard("Move to Live During Moderation");
		String itemFullName = context.getFullName("item");
		String itemFullName2 = itemFullName + " 2";
		wizard.editbox(1, itemFullName);
		wizard.save().submit();
		logout();

		// Login as SimpleModerator and moderate the item at the first step.
		logon("SimpleModerator", "``````");

		TaskListPage taskListPage = new TaskListPage(context).load();
		ModerateListSearchResults modResults = taskListPage.exactQuery(itemFullName);
		assertEquals(modResults.getStepName(itemFullName), "moderation step 1");
		modResults.moderate(itemFullName).accept();
		logout();

		// login as the owner of the item and new version it.
		logon("admin", "``````");

		wizard = SearchPage.searchAndView(context, itemFullName).adminTab().newVersion();
		wizard.editbox(1, itemFullName2);
		wizard.save().submit();

		// check that there is a version that is live, and one that is
		// moderating.
		ItemAdminPage adminPage = new ItemAdminPage(context).load();
		adminPage.exactQuery(itemFullName);
		adminPage.setSort("datemodified");
		ItemListPage itemList = adminPage.results();
		assertEquals(itemList.getResultForTitle(itemFullName, 1).getStatus(), "live");
		assertEquals(itemList.getResultForTitle(itemFullName2, 1).getStatus(), "moderating");
		logout();

		// Login as SimpleModerator and moderate the item at the first step.
		logon("SimpleModerator", "``````");

		taskListPage = new TaskListPage(context).load();
		modResults = taskListPage.exactQuery(itemFullName2);
		assertEquals(modResults.getStepName(itemFullName2), "moderation step 1");
		modResults.moderate(itemFullName2).accept();

		// After moderation, check that the item has gone life after the first
		// moderation step
		adminPage = new ItemAdminPage(context).load();
		adminPage.exactQuery(itemFullName2);
		adminPage.setSort("datemodified");
		itemList = adminPage.results();
		assertEquals(itemList.getResultForTitle(itemFullName2, 1).getStatus(), "live");
		logout();

		// Login as the SecondStepModerator and check that the item is still
		// there to moderate, and is still live once moderated from second step.
		logon("SecondStepModerator", "``````");
		taskListPage = new TaskListPage(context).load();
		modResults = taskListPage.exactQuery(itemFullName2);
		ModerationView tasksTab = modResults.moderate(itemFullName2);
		tasksTab.assignToMe();
		tasksTab.accept();
		logout();
	}

}
