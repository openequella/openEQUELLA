package com.tle.webtests.test.workflow.assignment;

/*
 * Tests re-assignment after rejection
 */
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.tasklist.ModerationView;
import com.tle.webtests.pageobject.tasklist.TaskListPage;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.test.AbstractCleanupTest;

@TestInstitution("workflow")
public class WorkflowAssignmentTest extends AbstractCleanupTest{
	
	@Test
	public void testWorkflow()
	{
		String itemFullName = context.getFullName("item");
		
		logon("admin", "``````");
		
		WizardPageTab wizard = new ContributePage(context).load().openWizard("Simple 3 Step");
		wizard.editbox(1, itemFullName);
		wizard.save().submit();
		
		//Assigned to same user
		TaskListPage taskList = new TaskListPage(context).load();
		ModerationView mv = taskList.exactQuery(itemFullName).moderate(itemFullName);
		assertFalse(mv.isAssignedToMe());
		
		mv.assignToMe();
		mv.accept(); //4
		
		taskList = new TaskListPage(context).load();
		mv = taskList.exactQuery(itemFullName).moderate(itemFullName);
		assertTrue(mv.isAssignedToMe());
		mv.assignToMe(); //Un-assign: Will work since it's the same button but might be flakey if someone messes with it
		mv.accept();

		taskList = new TaskListPage(context).load();
		mv = taskList.exactQuery(itemFullName).moderate(itemFullName);
		assertTrue(mv.isAssignedToMe());
		mv.assignToMe(); //un-assign
		mv.reject().rejectWithMessage("Reject",null);

		taskList = new TaskListPage(context).load();
		mv = taskList.exactQuery(itemFullName).moderate(itemFullName);
		assertTrue(mv.isAssignedToMe());
		
	}
}
