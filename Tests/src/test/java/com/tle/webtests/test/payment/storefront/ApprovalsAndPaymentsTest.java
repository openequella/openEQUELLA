package com.tle.webtests.test.payment.storefront;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.SettingsPage;
import com.tle.webtests.pageobject.payment.storefront.ApprovalsPage;
import com.tle.webtests.pageobject.payment.storefront.ApprovalsPage.ApprovalEditPage;
import com.tle.webtests.test.AbstractSessionTest;

/**
 * @author Dustin
 */

// Here we test the approval and payment rules page, we can only do so much as
// it relies on the recipient selector but would should be able to catch any
// major explosions (and get some extra coverage)

@TestInstitution("storefront")
public class ApprovalsAndPaymentsTest extends AbstractSessionTest
{
	private static final String BILL = "bill purchaser [OrderHistoryPurchaser]";
	private static final String JOHN = "johnny approver [OrderHistoryApprover]";
	private static final String AUTO = "Auto Test [AutoTest]";
	private static final String APPROVAL = "ApprovalTest User [ApprovalTestUser]";

	@Test
	public void approvalsTest()
	{
		logon("autotest", "automated");

		SettingsPage sp = new SettingsPage(context).load();
		ApprovalsPage ap = sp.approvalsPage();

		// Just check a few so we know the tables are working
		assertTrue(ap.rowExists(BILL, JOHN, true));
		assertTrue(ap.rowExists(AUTO, AUTO, true));
		assertTrue(ap.rowExists(APPROVAL, APPROVAL, true));
		ApprovalEditPage editPage = ap.edit(BILL, JOHN, true);
		assertEquals(editPage.getFirst(), BILL);
		assertEquals(editPage.getSecond(), JOHN);
		ap = editPage.cancel();

		assertTrue(ap.rowExists(AUTO, AUTO, false));
		assertTrue(ap.rowExists(APPROVAL, AUTO, false));
		assertTrue(ap.rowExists(JOHN, AUTO, false));
		editPage = ap.edit(APPROVAL, AUTO, false);
		assertEquals(editPage.getFirst(), APPROVAL);
		assertEquals(editPage.getSecond(), AUTO);
		ap = editPage.cancel();

		logout();
	}
}