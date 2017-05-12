package com.tle.webtests.test.workflow;


import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.tasklist.ModerationView;
import com.tle.webtests.pageobject.tasklist.TaskListPage;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.ModerationMessagePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.test.AbstractCleanupTest;

@TestInstitution("workflow")
public class DisabledControlsTest extends AbstractCleanupTest
{
	@Test
	public void disabledControlsTest()
	{
		String itemFullName = context.getFullName("item");

		logon("admin", "``````");

		for(int i =0; i < 3; i++) {
			WizardPageTab wizard = new ContributePage(context).load().openWizard("Simple 3 Step");
			wizard.editbox(1, itemFullName + " " + ( i + 1 ));
			wizard.save().submit();
		}

		TaskListPage taskList = new TaskListPage(context).load();
		ModerationView mv = taskList.exactQuery(itemFullName).moderate(itemFullName + " 2");
		assertFalse(mv.navigationDisabled() || mv.moderationDisabled());

		ModerationMessagePage messagePage = mv.acceptToMessagePage();
		assertTrue(mv.navigationDisabled() && mv.moderationDisabled());
		messagePage.cancel();

		messagePage = mv.reject();
		assertTrue(mv.navigationDisabled() && mv.moderationDisabled());
		messagePage.cancel();

		messagePage = mv.postComment();
		assertTrue(mv.navigationDisabled() && mv.moderationDisabled());
		messagePage.cancel();

		logout();
	}
}
