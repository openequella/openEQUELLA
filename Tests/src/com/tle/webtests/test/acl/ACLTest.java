package com.tle.webtests.test.acl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.ErrorPage;
import com.tle.webtests.pageobject.generic.page.VerifyableAttachment;
import com.tle.webtests.pageobject.searching.BulkResultsPage;
import com.tle.webtests.pageobject.searching.ItemAdminPage;
import com.tle.webtests.pageobject.searching.SearchPage;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import com.tle.webtests.test.AbstractCleanupAutoTest;

@TestInstitution("acl")
public class ACLTest extends AbstractCleanupAutoTest
{
	@Test
	public void testPurgeItemACL()
	{
		final String ALLOW_PURGE_U = "purge_grant";
		final String DENY_PURGE_U = "purge_deny";
		final String PW = "``````";
		final String ITEM_NAME = "PURGE_ACL test item";
		// log in as deny
		logon(DENY_PURGE_U, PW);
		ItemAdminPage mrp = new ItemAdminPage(context).load();
		SummaryPage item = mrp.viewItem(ITEM_NAME);
		Assert.assertFalse(item.hasAction("Purge from EQUELLA"));
		mrp = new ItemAdminPage(context).load();
		mrp.search(ITEM_NAME);
		BulkResultsPage brp = mrp.bulk().selectAll().executeCommandFailure("purge");
		Assert.assertFalse(brp.noErrors());
		// log in as allow
		logon(ALLOW_PURGE_U, PW);
		mrp = new ItemAdminPage(context).load();
		item = mrp.viewItem(ITEM_NAME);
		Assert.assertTrue(item.hasAction("Purge from EQUELLA"));
		mrp = new ItemAdminPage(context).load();
		mrp.search(ITEM_NAME);
		brp = mrp.bulk().selectAll().executeCommandFailure("purge");
		Assert.assertTrue(brp.noErrors());
	}

	@Test
	public void testViewAttachmentACL()
	{
		final String ALLOW_VIEW_ATTACHMENT_USER = "view_attachment_grant";
		final String DENY_VIEW_ATTACHMENT_USER = "view_attachment_revoke";
		final String PW = "``````";
		final String ITEM_NAME = "web page item";

		// No VIEW_ITEM and no VIEW_ATTACHMENTS privilege
		logon(DENY_VIEW_ATTACHMENT_USER, PW);

		SummaryPage summary = SearchPage.searchAndView(context, ITEM_NAME);
		assertFalse(summary.hasAttachmentsSection());
		goToAttachmentDirect();
		ErrorPage error = new ErrorPage(context);
		assertEquals(error.getMainErrorMessage(), "Access denied");

		logout();

		// No VIEW_ITEM, but VIEW_ATTACHMENTS privilege
		logon(ALLOW_VIEW_ATTACHMENT_USER, PW);
		summary = SearchPage.searchAndView(context, ITEM_NAME);
		assertFalse(summary.hasAttachmentsSection());
		goToAttachmentDirect();
		assertTrue(new VerifyableAttachment(context).isVerified());

		logout();

		// VIEW_ITEM privilege
		logon(AUTOTEST_LOGON, AUTOTEST_PASSWD);
		summary = SearchPage.searchAndView(context, ITEM_NAME);
		assertTrue(summary.hasAttachmentsSection());
		goToAttachmentDirect();
		assertTrue(new VerifyableAttachment(context).isVerified());

		logout();
	}

	private void goToAttachmentDirect()
	{
		final String ITEM_URL = "items/5ca17142-32ab-43b8-8ee8-ccc5c6a7e1ad/1/?attachment.uuid=b7fa9698-4e25-4965-b902-c40f50e454d9&attachment.stream=true";
		context.getDriver().get(context.getBaseUrl() + ITEM_URL);
	}
}
