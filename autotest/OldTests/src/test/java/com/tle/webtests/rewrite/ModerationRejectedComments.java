package com.tle.webtests.rewrite;


import org.testng.Assert;
import org.testng.annotations.Test;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.myresources.MyResourcesPage;
import com.tle.webtests.pageobject.tasklist.NotificationSearchResults;
import com.tle.webtests.pageobject.tasklist.NotificationsPage;
import com.tle.webtests.pageobject.viewitem.ModerationHistoryPage;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import com.tle.webtests.test.AbstractSessionTest;


@TestInstitution("dinuk")
public class ModerationRejectedComments extends AbstractSessionTest{

	@Test
	public void viewRejectedComments()
	{
		logon("AutoTest", "automated");
		
		//Access Moderation Queue of My Resources Page
		MyResourcesPage myResourcesPage = new MyResourcesPage(context, null).load();
		myResourcesPage.clickSelectedTab("Moderation queue");
		
		//Verify Show Comment Link Presence
		Assert.assertTrue(myResourcesPage.isShowCommentLinkPresentAtIndex(1));
		
		//Access Show Comment Pop-up Dialog and Verify the Comment Text
		Assert.assertEquals(myResourcesPage.accessShowCommentViewAtIndex(1), ("Rejected Item C Reason"));
		
		//Access Item Summary
		SummaryPage summaryTabPage = myResourcesPage.accessItemSummeryInModQueueItemAtIndex(1);
		
		//Access Moderation History.
		ModerationHistoryPage moderationHistoryPage = summaryTabPage.viewItemModerationHistory();
		
		//Verify 'Show Comment' Link Presence
		Assert.assertTrue(moderationHistoryPage.verifyShowCommentAtIndex(1));
		
		//Verify Event Display Sequence
		Assert.assertTrue(moderationHistoryPage.eventAtIndex(1).equals("Rejected at task Mod1 (Show comment)"));
		
		//Access Notifications Page
		NotificationsPage notificationsPage = new NotificationsPage(context).load();
		
		//Access Results
		NotificationSearchResults moderateListSearchResults = notificationsPage.results();
		
		//Verify the non-display of the rejected reason
		Assert.assertTrue(moderateListSearchResults.isReasonRejected("Rejected Item C"));
	}
	
	
}
