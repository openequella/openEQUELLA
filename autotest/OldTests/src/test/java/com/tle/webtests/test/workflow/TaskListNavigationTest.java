package com.tle.webtests.test.workflow;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.tasklist.ModerationView;
import com.tle.webtests.pageobject.tasklist.TaskListPage;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.test.AbstractCleanupTest;

@TestInstitution("workflow")
public class TaskListNavigationTest extends AbstractCleanupTest
{
	private String taskInfoMsg(int x, int y)
	{
		return x+" of "+y+" tasks";
	}
	@Test
	public void taskListNavigation()
	{
		logon("simplemoderator", "``````");
		WizardPageTab wizard = new ContributePage(context).load().openWizard("Simple 1 Step");
		wizard.editbox(1, context.getFullName("Item 1"));
		wizard.save().submit();
		wizard = new ContributePage(context).load().openWizard("Simple 1 Step");
		wizard.editbox(1, context.getFullName("Item 2"));
		wizard.save().submit();
		wizard = new ContributePage(context).load().openWizard("Simple 1 Step");
		wizard.editbox(1, context.getFullName("Item 3"));
		wizard.save().submit();

		TaskListPage tlp = new TaskListPage(context).load();
		tlp.setSort("name");
		ModerationView mv = tlp.exactQuery(namePrefix).moderate(context.getFullName("Item 1"));
		assertEquals(mv.getTaskNavigationInfo(), taskInfoMsg(1, 3));
		mv.accept();
		tlp = new TaskListPage(context).load();
		mv = tlp.exactQuery(namePrefix).moderate(context.getFullName("Item 2"));
		assertEquals(mv.getTaskNavigationInfo(), taskInfoMsg(1,2));
		mv = mv.navigateNext();
		assertEquals(mv.getTaskNavigationInfo(), taskInfoMsg(2,2));
		mv = mv.navigatePrev();
		assertEquals(mv.getTaskNavigationInfo(), taskInfoMsg(1, 2));
		mv.accept();
		tlp = new TaskListPage(context).load();
		mv = tlp.exactQuery(namePrefix).moderate(context.getFullName("Item 3"));
		assertEquals(mv.getTaskNavigationInfo(), "Task list");
		assertTrue(mv.navigationDisabled());
	}
}
