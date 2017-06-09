package com.tle.webtests.test.payment.storefront;

import static org.testng.Assert.assertFalse;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.NotPrefixedName;
import com.tle.webtests.pageobject.PrefixedName;
import com.tle.webtests.pageobject.payment.storefront.CartViewPage;
import com.tle.webtests.pageobject.payment.storefront.OrderPage;
import com.tle.webtests.pageobject.payment.storefront.ShopPage;
import com.tle.webtests.test.AbstractCleanupTest;
import com.tle.webtests.test.payment.global.RegisterStoreAndStoreFront;

/**
 * @author Seb (then Dustin went and broke it)
 * @see DTEC: #018019, #018014
 */

@TestInstitution("storefront")
public class OrderHistoryTest extends AbstractCleanupTest
{
	private static final String STORE_NAME = RegisterStoreAndStoreFront.STORE_NAME;
	private static final String STORE_NAME2 = RegisterStoreAndStoreFront.STORE_NAME2;
	private static final String STORE2_ITEM = "Outright and Subscribe Item";
	private static final String PURCHASER_COMMENT = "Please sir approve my item please";
	private static final String REJECTION_COMMENT = "/TLE Automated Tests/src/com/tle/webtests/test/files/Special characters - Ñ…Ñ†Ñ‡ test2.jpg";
	private static final String RESUBMIT_COMMENT = "<h3>for real this time<h3><br/><sript>alert('hello');</script>";
	private static final String APPROVAL_COMMENT = "gj buddy, here's some weird characters: §¶ß»‹›«‘’“”‚&lt;";
	private static final String REDRAFT_COMMENT = "I have full faith in our string escaping";
	private static final String APPROVER = "OrderHistoryApprover";
	private static final String PURCHASER = "OrderHistoryPurchaser";
	private static final String PURCHASER_NAME = "bill purchaser";
	private static final PrefixedName CATALOGUE = new NotPrefixedName("cat1");

	@Test
	public void setupOrderHistory()
	{
		logon(PURCHASER, "``````");
		// submit -> reject -> resubmit -> check
		CartViewPage cart = new ShopPage(context).load().pickStoreSingleCatalogue(STORE_NAME, CATALOGUE)
			.search(namePrefix).getResult(1).viewSummary().addToCart().viewCart();
		cart.setComment(PURCHASER_COMMENT).submitForApproval();
		logout();

		logon(APPROVER, "``````");
		OrderPage approvalPage = new ShopPage(context).load().selectOrderForApproval(PURCHASER_NAME);
		approvalPage.addComment(REJECTION_COMMENT).rejectOrder();
		logout();

		logon(PURCHASER, "``````");
		OrderPage orderPage = new ShopPage(context).load().selectOrder(1);
		orderPage.addComment(RESUBMIT_COMMENT).submitOrder();
		logout();

		logon(APPROVER, "``````");
		approvalPage = new ShopPage(context).load().selectOrderForApproval(PURCHASER_NAME);
		approvalPage.addComment(APPROVAL_COMMENT).approveOrder();
		logout();

	}

	@Test(dependsOnMethods = {"setupOrderHistory"})
	public void checkOrderHistory()
	{
		final String SUBMIT_FOR_APPROVAL = "Submitted for approval";
		final String REJECTED = "Rejected";
		final String PAYMENT_SUBMIT = "Submitted for payment";

		logon(PURCHASER, "``````");
		OrderPage orderPage = new ShopPage(context).load().selectOrder(1);
		Assert.assertEquals(orderPage.getCommentType(4), SUBMIT_FOR_APPROVAL);
		Assert.assertEquals(orderPage.getCommentText(4), PURCHASER_COMMENT);
		Assert.assertEquals(orderPage.getCommentType(3), REJECTED);
		Assert.assertEquals(orderPage.getCommentText(3), REJECTION_COMMENT);
		Assert.assertEquals(orderPage.getCommentType(2), SUBMIT_FOR_APPROVAL);
		Assert.assertEquals(orderPage.getCommentText(2), RESUBMIT_COMMENT);
		Assert.assertEquals(orderPage.getCommentType(1), PAYMENT_SUBMIT);
		Assert.assertEquals(orderPage.getCommentText(1), APPROVAL_COMMENT);

		logout();
	}

	@Test(dependsOnMethods = {"checkOrderHistory"})
	public void redraftAndMultiCurrencyTest()
	{
		logon("autotest", "automated");
		new ShopPage(context).load().selectOrderForPayment(PURCHASER_NAME).addComment("redraft this").rejectOrder();
		logout();
		logon(PURCHASER, "``````");
		new ShopPage(context).load().selectOrder(1).addComment(REDRAFT_COMMENT).redraftOrder();

		CartViewPage cart = new ShopPage(context).load().pickStoreSingleCatalogue(STORE_NAME2, CATALOGUE)
			.search(STORE2_ITEM).getResult(1).viewSummary().addToCart().viewCart();
		cart.submitForApproval();

		logon(APPROVER, "``````");
		ShopPage sp = new ShopPage(context).load();
		Assert.assertEquals(sp.getTotalForOrder(PURCHASER_NAME), "Multi currency");
		OrderPage op = sp.selectOrderForApproval(PURCHASER_NAME);
		Assert.assertFalse(op.commentExists(2));
		// All the others should be gone
		new ShopPage(context).load().selectOrderForApproval(PURCHASER_NAME).addComment("test deletion").rejectOrder();
		logout();
		logon(PURCHASER, "``````");
		sp = new ShopPage(context).load().selectOrder(1).deleteOrder();
		assertFalse(sp.isOrderPresent(1), "Order is still there after being deleted");
		logout();
	}

	@Override
	protected void cleanupAfterClass() throws Exception
	{
		logon(APPROVER, "``````");
		ShopPage sp = new ShopPage(context).load();
		if( sp.isOrderPresent(1) )
		{
			new ShopPage(context).load().selectOrderForApproval(PURCHASER_NAME).addComment("cleanup").rejectOrder();
			logout();
			logon(PURCHASER, "``````");
			new ShopPage(context).load().selectOrder(1).deleteOrder();
		}
	}
}
