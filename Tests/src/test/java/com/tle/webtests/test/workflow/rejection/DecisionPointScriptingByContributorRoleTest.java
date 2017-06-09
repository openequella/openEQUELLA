package com.tle.webtests.test.workflow.rejection;

import org.testng.annotations.Test;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.searching.ModerateListSearchResults;
import com.tle.webtests.pageobject.tasklist.ModerationView;
import com.tle.webtests.pageobject.tasklist.TaskListPage;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.test.AbstractCleanupTest;

@TestInstitution("workflow")
public class DecisionPointScriptingByContributorRoleTest extends AbstractCleanupTest
{

	public DecisionPointScriptingByContributorRoleTest()
	{
		super("DPSBCRT");
		setDeleteCredentials("admin", "``````");
	}

	@Test
	public void scriptingByContributorRole()
	{

		// Login as a user not in the System Administrator role, and contribute
		// an item.
		logon("admin", "``````");

		WizardPageTab wizard = new ContributePage(context).load().openWizard(
			"Decision Point Scripting by Contributor Role");
		String itemFullName = context.getFullName("item");
		wizard.editbox(1, itemFullName);
		wizard.save().submit();
		logout();

		// Login as the Moderator, who is also a member of the System
		// Administrator role
		// Moderate the item
		logon("SimpleModerator", "``````");
		TaskListPage taskListPage = new TaskListPage(context).load();
		ModerateListSearchResults modResults = taskListPage.exactQuery(itemFullName);
		ModerationView tasksTab = modResults.moderate(itemFullName);
		tasksTab.assignToMe();
		tasksTab.accept();

		// While still logged in as this Administrator user, contribute an item
		wizard = new ContributePage(context).load().openWizard("Decision Point Scripting by Contributor Role");
		String itemFullName2 = context.getFullName("item2");
		wizard.editbox(1, itemFullName2);
		wizard.save().submit();
		logout();

	}

}
