package com.tle.webtests.test.workflow;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.tasklist.ManageTasksPage;
import com.tle.webtests.pageobject.tasklist.ModerationView;
import com.tle.webtests.pageobject.tasklist.TaskListPage;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.test.AbstractCleanupTest;

@TestInstitution("workflow")
public class ManageTasksTest extends AbstractCleanupTest
{

	//might use data provider for item creation
	@Test
	public void manageWorkflowTest()
	{
		String ITEM_1 = context.getFullName("Workflow 1");
		String ITEM_2 = context.getFullName("Workflow 2, Step 1");
		String ITEM_3 = context.getFullName("Workflow 2, Step 2");

		logon("TLE_ADMINISTRATOR", "tle010");
		WizardPageTab wizard = new ContributePage(context).load().openWizard("Simple 1 Step");
		wizard.editbox(1, ITEM_1);
		wizard.save().submit();
		wizard = new ContributePage(context).load().openWizard("Simple 2 Step");
		wizard.editbox(1, ITEM_2);
		wizard.save().submit();
		wizard = new ContributePage(context).load().openWizard("Simple 2 Step");
		wizard.editbox(1, ITEM_3);
		wizard.save().submit();
		logon("simplemoderator", "``````");
		TaskListPage taskList = new TaskListPage(context).load();
		ModerationView mv = taskList.exactQuery(ITEM_3).moderate(ITEM_3);
		mv.accept();

		//testing roles + permissions
		logon("admin", "``````"); //admin only has MANAGE_WORKFLOW Permission for workflow 1 item
		ManageTasksPage manageTasks = new ManageTasksPage(context).load();
		manageTasks.search(namePrefix);
		assertTrue(manageTasks.results().doesResultExist(ITEM_1));
		assertFalse(manageTasks.results().doesResultExist(ITEM_2)
			&& manageTasks.results().doesResultExist(ITEM_3));

		logon("TLE_ADMINISTRATOR", "tle010");// tle_admin has all
												// MANAGE_WORKFLOW permissions
		manageTasks = new ManageTasksPage(context).load();
		manageTasks.search(namePrefix);
		assertTrue(manageTasks.hasResults());
		assertTrue(manageTasks.results().doesResultExist(ITEM_1));
		assertTrue(manageTasks.results().doesResultExist(ITEM_2));

		//testing sorting
		manageTasks.get();
		manageTasks.setSort("name");
		assertTrue(manageTasks.ensureSortSelected("name"));
		manageTasks.setSort("priority");
		assertTrue(manageTasks.ensureSortSelected("priority"));
		manageTasks.setSort("waiting");
		assertTrue(manageTasks.ensureSortSelected("waiting"));
		manageTasks.setSort("duedate");
		assertTrue(manageTasks.ensureSortSelected("duedate"));

		//testing filtering
		manageTasks.searchWithinWorkflow("46392820-5bce-3d29-b4b3-61131cfe20a4"); //"2 Step Workflow" id 
		manageTasks.setFilter("39730e60-fe34-66ae-0e97-7c2a2fe0e134"); // step 1
		assertTrue(manageTasks.results().doesResultExist(ITEM_2));
		manageTasks.setFilter("f971d8a4-5639-3e23-c32c-535fa93ced53"); //step 2
		assertTrue(manageTasks.results().doesResultExist(ITEM_3));
		manageTasks = manageTasks.clearFilters();

		// moderator for step one items
		manageTasks.selectModerator("moderator", "SimpleModerator");
		assertTrue(manageTasks.results().doesResultExist(ITEM_2));
		// moderator for step two items
		manageTasks.selectModerator("moderator", "SecondStepModerator");
		assertTrue(manageTasks.results().doesResultExist(ITEM_3));
		manageTasks.clearModerator();
		assertTrue(manageTasks.results().doesResultExist(ITEM_3)
			&& manageTasks.results().doesResultExist(ITEM_2));

	}
}
