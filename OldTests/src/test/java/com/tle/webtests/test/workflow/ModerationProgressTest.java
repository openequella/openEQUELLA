package com.tle.webtests.test.workflow;

import static org.testng.Assert.assertTrue;

import java.text.ParseException;
import java.util.Date;

import org.testng.annotations.Test;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.searching.SearchPage;
import com.tle.webtests.pageobject.tasklist.ModerationView;
import com.tle.webtests.pageobject.tasklist.TaskListPage;
import com.tle.webtests.pageobject.viewitem.ModerationTab;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import com.tle.webtests.pageobject.wizard.ConfirmationDialog;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.test.AbstractCleanupTest;

@TestInstitution("workflow")
public class ModerationProgressTest extends AbstractCleanupTest
{
	@Test
	public void ModerationProgress() throws ParseException
	{
		logon("admin", "``````");
		WizardPageTab wizard = new ContributePage(context).load().openWizard("Parallel Workflow Collection");
		wizard.editbox(1, context.getFullName("moderated item"));
		SummaryPage finished = wizard.save().submit().get();
		// Testing tasks information correct/ time in moderation
		ModerationTab modTab = finished.moderationTab();
		assertTrue(modTab.isTaskPresent("Task 1"));
		assertTrue(modTab.isTaskPresent("Task 2"));
		assertTrue(modTab.moderatorCorrect(1, "Simple Moderator"));
		assertTrue(modTab.moderatorCorrect(2, "Second Step"));
		// was created at most 5 minutes ago
		long fiveMinutes = 300000;
		Date now = new Date();
		long fiveMinutesFuture = now.getTime() + fiveMinutes;
		long waitTime = modTab.waitTime();
		assertTrue(fiveMinutesFuture > waitTime, "fiveMinutesFuture: " + fiveMinutesFuture + " + waitTime: " + waitTime);

	}

	@Test
	public void ModerationCommentsFromProgress()
	{

		logon("admin", "``````");
		String itemName = context.getFullName("Comment Item");
		WizardPageTab wizard = new ContributePage(context).load().openWizard("Simple 2 Step with Rejection Points");
		wizard.editbox(1, itemName);
		ConfirmationDialog confirm = wizard.save();
		// contribution comment
		confirm.addModerationComment("Contribution Comment");
		confirm.submit();
		// moderation comment
		logon("simplemoderator", "``````");
		TaskListPage tlp = new TaskListPage(context).load();
		ModerationView mv = tlp.exactQuery(itemName).moderate(itemName);
		mv.postComment().addModerationComment("Moderation Comment"); // cancel
																				// to
																				// get
																				// out
		// check comments there
		SearchPage sp = new SearchPage(context).load();
		sp.setIncludeNonLive(true);
		SummaryPage summaryPage = sp.search(itemName).getResultForTitle(itemName, 1).viewSummary();
		ModerationTab modTab = summaryPage.moderationTab();
		modTab.openModerationComments(1);
		assertTrue(modTab.containsComment("Moderation Comment"));
		assertTrue(modTab.containsComment("Contribution Comment"));
		assertTrue(modTab.getCommentClass("Moderation Comment").equalsIgnoreCase("comment "));
		assertTrue(modTab.getCommentClass("Contribution Comment").equalsIgnoreCase("comment "));
		assertTrue(modTab.getNumberModerationComments(1) == 2);

		// acceptance comment
		tlp = new TaskListPage(context).load();
		mv = tlp.exactQuery(itemName).moderate(itemName);
		mv.acceptToMessagePage().acceptWithMessage("Acceptance Message");
		// check only acceptance comment there
		sp = new SearchPage(context).load();
		sp.setIncludeNonLive(true);
		summaryPage = sp.search(itemName).getResultForTitle(itemName, 1).viewSummary();
		modTab = summaryPage.moderationTab();
		assertTrue(modTab.getNumberModerationComments(1) == 1);
		modTab.openModerationComments(1);
		assertTrue(modTab.containsComment("Acceptance Message"));
		assertTrue(modTab.getCommentClass("Acceptance Message").equalsIgnoreCase("comment approval"));

		// reject
		logon("secondstepmoderator", "``````");
		tlp = new TaskListPage(context).load();
		mv = tlp.exactQuery(itemName).moderate(itemName);
		mv.reject().rejectWithMessage("Reject Message", "First Step");
		// check only reject comment there
		sp = new SearchPage(context).load();
		sp.setIncludeNonLive(true);
		summaryPage = sp.search(itemName).getResultForTitle(itemName, 1).viewSummary();
		modTab = summaryPage.moderationTab();
		assertTrue(modTab.getNumberModerationComments(1) == 1);
		modTab.openModerationComments(1);
		assertTrue(modTab.containsComment("Reject Message"));
		assertTrue(modTab.getCommentClass("Reject Message").equalsIgnoreCase("comment rejection"));
	}
}
