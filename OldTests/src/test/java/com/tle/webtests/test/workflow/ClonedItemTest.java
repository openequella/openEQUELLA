package com.tle.webtests.test.workflow;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.searching.BulkSection;
import com.tle.webtests.pageobject.searching.ItemAdminPage;
import com.tle.webtests.pageobject.searching.ItemListPage;
import com.tle.webtests.pageobject.searching.SearchPage;
import com.tle.webtests.pageobject.tasklist.ModerationView;
import com.tle.webtests.pageobject.tasklist.TaskListPage;
import com.tle.webtests.pageobject.viewitem.MoveCloneDialog;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.test.AbstractCleanupTest;

@TestInstitution("workflow")
public class ClonedItemTest extends AbstractCleanupTest{

	@Test
	public void moderatingClonedTest()
	{
		final int NUMBER_OF_ITEMS = 3;
		String[] itemFullNames = new String[NUMBER_OF_ITEMS];

		logon("admin", "``````");
		//Contribute 3 items
		for(int i = 0; i < NUMBER_OF_ITEMS; i++) {
			itemFullNames[i] = context.getFullName("item-" + i);

			WizardPageTab wizard = new ContributePage(context).load().openWizard("Simple 1 Step");
			wizard.editbox(1, itemFullNames[i]);
			wizard.save().submit();
		}
		
		logout();
		
		//Item 1 moderating (do nothing)
		
		//Item 2 reject
		int currentItem = 1;
		logon("SimpleModerator", "``````");
		TaskListPage taskList = new TaskListPage(context).load();
		ModerationView mv = taskList.exactQuery(itemFullNames[currentItem]).moderate(itemFullNames[currentItem]);
		mv.reject().rejectWithMessage("Reject", null);
		
		//Item 3 approve and review
		currentItem = 2;
		taskList = new TaskListPage(context).load();
		mv = taskList.exactQuery(itemFullNames[currentItem]).moderate(itemFullNames[currentItem]);
		mv.accept();
		SearchPage.searchAndView(context, itemFullNames[currentItem]).adminTab().review();

		//clone all
		ItemAdminPage filterListPage = new ItemAdminPage(context).load();
		ItemListPage filterResults = filterListPage.all().search(context.getFullName("item"));
		
		assertTrue( filterResults.isResultsAvailable());
		BulkSection bulk = filterListPage.bulk();
		MoveCloneDialog cloneDialog = bulk.selectAll().clone();
		cloneDialog.setTargetCollection("Simple 1 Step");
		cloneDialog.executeBulk().waitAndFinish(filterListPage);
		logout();
		
		//approve all 6
		//#2 original (rejected)
		logon("admin", "``````");
		filterListPage = new ItemAdminPage(context).load();
		filterResults = filterListPage.all().search(context.getFullName("item"));
		assertTrue( filterResults.isResultsAvailable());
		filterListPage.filterByStatus("rejected");
		bulk = filterListPage.bulk();
		bulk.commandAll("redraft");
		
		//Submit all drafts (the 3 clones, and the re-drafted reject)
		filterListPage = new ItemAdminPage(context).load();
		filterResults = filterListPage.all().search(context.getFullName("item"));
		filterListPage.filterByStatus("draft");
		bulk = filterListPage.bulk();
		bulk.commandAll("submit");

		logout();
		
		//Approve everything
		logon("SimpleModerator","``````");

		taskList = new TaskListPage(context).load();
		
		for(int i = 0; i < NUMBER_OF_ITEMS; i++) {
			taskList.search(context.getFullName("item")).moderate(itemFullNames[i]).accept();
			taskList.search(context.getFullName("item")).moderate(itemFullNames[i]).accept();
			assertFalse(taskList.search(context.getFullName("item")).doesResultExist(itemFullNames[i]));
		}
		
		logout();
	}
}
