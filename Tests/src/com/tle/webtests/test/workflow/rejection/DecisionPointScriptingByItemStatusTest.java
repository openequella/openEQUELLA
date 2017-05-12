package com.tle.webtests.test.workflow.rejection;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.searching.ModerateListSearchResults;
import com.tle.webtests.pageobject.searching.SearchPage;
import com.tle.webtests.pageobject.tasklist.TaskListPage;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.test.AbstractCleanupTest;

@TestInstitution("workflow")
public class DecisionPointScriptingByItemStatusTest extends AbstractCleanupTest
{

	public DecisionPointScriptingByItemStatusTest()
	{
		super("DPSBIST");
		setDeleteCredentials("admin", "``````");
	}

	@Test
	public void scriptingByItemStatus()
	{

		// Contribute an item and submit it to the workflow.
		logon("admin", "``````");

		WizardPageTab wizard = new ContributePage(context).load().openWizard("Decision Point Scripting by Item Status");
		String itemFullName = context.getFullName("item");
		wizard.editbox(1, itemFullName);
		wizard.save().submit();
		logout();

		// login as the moderator and accept the item
		logon("SecondStepModerator", "``````");
		TaskListPage taskListPage = new TaskListPage(context).load();
		ModerateListSearchResults modResults = taskListPage.exactQuery(itemFullName);
		assertEquals(modResults.getStepName(itemFullName), "This item is in moderation, please moderate it.");
		modResults.moderate(itemFullName).accept();

		// While still logged in as the moderator, send the item to review via
		// the Administer tab
		SearchPage.searchAndView(context, itemFullName).adminTab().review();
		logout();

		// Login as the original contributor, and edit the item
		logon("admin", "``````");
		SearchPage.searchAndView(context, itemFullName).adminTab().edit().saveNoConfirm();
		logout();

		// Login as the moderator assigned to handle tasks with review status
		logon("SimpleModerator", "``````");
		taskListPage = new TaskListPage(context).load();
		modResults = taskListPage.exactQuery(itemFullName);

		// Check that the item is in review, and accept the item.
		assertEquals(modResults.getStepName(itemFullName), "This item is in review, review me.");
		modResults.moderate(itemFullName).accept();
		logout();
	}

}
