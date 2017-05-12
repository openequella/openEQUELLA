package com.tle.webtests.test.payment.backend;

import static org.testng.Assert.assertEquals;

import java.util.Calendar;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.NotPrefixedName;
import com.tle.webtests.pageobject.PrefixedName;
import com.tle.webtests.pageobject.payment.backend.SalesHistoryPage;
import com.tle.webtests.pageobject.payment.storefront.CartViewPage;
import com.tle.webtests.pageobject.payment.storefront.CatalogueResourcePage;
import com.tle.webtests.pageobject.payment.storefront.OrderPage;
import com.tle.webtests.pageobject.payment.storefront.ShopPage;
import com.tle.webtests.pageobject.searching.SearchPage;
import com.tle.webtests.test.AbstractCleanupTest;
import com.tle.webtests.test.payment.global.RegisterStoreAndStoreFront;

/**
 * @author Seb
 * @see DTEC: #018021
 */

@TestInstitution("storefront")
public class SalesHistoryTest extends AbstractCleanupTest
{
	private static final String STORE_NAME = RegisterStoreAndStoreFront.STORE_NAME;
	private static final String ORIGINAL_STORE_NAME = RegisterStoreAndStoreFront.STOREFRONT_NAME;

	private static final String SUB_PRICE = "$7,700.00 AUD";
	private static final String PUR_PRICE = "$750.00 AUD";
	private static final PrefixedName CATALOGUE = new NotPrefixedName("cat1");

	@Test
	public void purchaseItem()
	{
		logon("SalesHistoryUser", "``````");
		CatalogueResourcePage item = new ShopPage(context).load().pickStoreSingleCatalogue(STORE_NAME, CATALOGUE)
			.search(namePrefix).getResult(1).viewSummary();
		item.setSubscriptionModel();
		item.setNumberOfUsers(true, "77");
		item.changeDuration(3);
		item.selectOtherDate();
		Calendar otherDate = Calendar.getInstance();
		otherDate.set(Calendar.DATE, 1);
		otherDate.set(Calendar.MONTH, 1);
		otherDate.set(Calendar.YEAR, 2015);
		item.setOtherDate(otherDate);
		assertEquals(item.getTotal(), SUB_PRICE);
		CartViewPage cart = item.addToCart().viewCart();
		OrderPage orderPage = cart.getStoreSection(STORE_NAME).payWithDemoGateway();
		orderPage.waitForStatus("Paid");

		item = new ShopPage(context).load().pickStoreSingleCatalogue(STORE_NAME, CATALOGUE).search(namePrefix)
			.getResult(1).viewSummary();
		item.setPurchaseModel();
		item.setNumberOfUsers(false, "150");
		assertEquals(item.getTotal(), PUR_PRICE);
		cart = item.addToCart().viewCart();
		cart.getStoreSection(STORE_NAME).payWithDemoGateway();
	}

	@Test(dependsOnMethods = {"purchaseItem"})
	public void checkSalesHistory()
	{
		PageContext context = newContext("storebackendssl");
		logon(context, "autotest", "automated");
		SalesHistoryPage history = new SearchPage(context).load().search(namePrefix).getResult(1).viewSummary()
			.viewSalesHistory();
		// should be in order of the outright purchase first then subscription.
		Assert.assertEquals(history.getStoreFrontForIndex(1), ORIGINAL_STORE_NAME);
		Assert.assertEquals(history.getTransactionForIndex(1), "Purchase");
		Assert.assertEquals(history.getPriceForIndex(1), PUR_PRICE);
		Assert.assertEquals(history.getStoreFrontForIndex(2), ORIGINAL_STORE_NAME);
		Assert.assertEquals(history.getTransactionForIndex(2), "Subscription");
		Assert.assertEquals(history.getPriceForIndex(2), SUB_PRICE);

		history = new SearchPage(context).load().search("OrderHistoryTest item").getResult(1).viewSummary()
			.viewSalesHistory();

		Assert.assertTrue(history.hasNoHistory());

		logout(context);
	}
}
