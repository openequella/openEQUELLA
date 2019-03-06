package com.tle.webtests.test.viewing;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

import org.testng.annotations.Test;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.searching.SearchPage;
import com.tle.webtests.pageobject.tasklist.TaskListPage;
import com.tle.webtests.pageobject.viewitem.ModerationHistoryPage;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.test.AbstractCleanupTest;

@TestInstitution("workflow")
public class ItemHistoryTest extends AbstractCleanupTest
{
	
	public ItemHistoryTest()
	{
		setDeleteCredentials("SimpleModerator", "``````");
	}

	@Test
	public void history()
	{
		String fullName = context.getFullName("item");
		String message = "accepted message";

		logon("SimpleModerator", "``````");
		WizardPageTab wizard = new ContributePage(context).load().openWizard("Simple 1 Step");

		wizard.editbox(1, fullName);
		wizard.save().submit();
		TaskListPage tasks = new TaskListPage(context).load();

		tasks.exactQuery(fullName).getResultForTitle(fullName, 1).moderate().acceptToMessagePage()
			.acceptWithMessage(message);

		SummaryPage summary = SearchPage.searchExact(context, fullName).getResultForTitle(fullName, 1).viewSummary();
		summary.review();

		ModerationHistoryPage history = summary.history();

		assertFalse(history.isShowEdits());
		assertFalse(history.isShowAllDetails());

		assertEquals(history.eventCount(), 3);

		assertEquals(history.eventAtIndex(1), "State changed to Review");
		assertEquals(history.eventAtIndex(2), "Went live");
		assertEquals(history.eventAtIndex(3), "Contributed");

		history.setShowEdits(true);
		assertEquals(history.eventCount(), 4);
		assertEquals(history.eventAtIndex(3), "Edited");

		history.setShowAllDetails(true);
		assertEquals(history.eventCount(), 8);
		assertEquals(history.eventAtIndex(1), "Workflow reset");
		assertEquals(history.eventAtIndex(2), "State changed to Review");
		assertEquals(history.eventAtIndex(4), "Accepted for task Only Step (Show comment)");
		assertEquals(history.eventAtIndex(6), "Submitted for moderation");
		assertEquals(history.commentAtIndex(4), message);
	}
}
