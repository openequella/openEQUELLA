package com.tle.webtests.test.payment.storefront;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.payment.storefront.BrowseCataloguePage;
import com.tle.webtests.pageobject.payment.storefront.CartViewPage;
import com.tle.webtests.pageobject.payment.storefront.CatalogueResourcePage;
import com.tle.webtests.pageobject.payment.storefront.ShopPage;
import com.tle.webtests.test.AbstractCleanupTest;
import com.tle.webtests.test.payment.global.RegisterStoreAndStoreFront;

/**
 * @see DTEC: #018007
 * @author Seb
 */
// Didn't test control disabling or cost total accuracy here
@TestInstitution("storefront")
public class NumberOfUsersDefaultsTest extends AbstractCleanupTest
{
	private static final String STORE_NAME = RegisterStoreAndStoreFront.STORE_NAME;
	private static final String SUBSCRIPTION_ITEM = "Subscription Item";
	private static final String OUTRIGHT_ITEM = "Purchase Outright Item";

	@Test
	public void testUserNumberDefaults()
	{
		logon("NumberUserDefault", "``````");

		BrowseCataloguePage browseCataloguePage = new ShopPage(context).load().pickStoreSingleCatalogue(STORE_NAME);
		browseCataloguePage.setQuery(SUBSCRIPTION_ITEM);
		CatalogueResourcePage page = browseCataloguePage.search().getResult(1).viewSummary();

		assertTrue(page.isSubscription());
		// 1 should be the default
		assertEquals(page.getNumberOfUsers(true), 1);
		page.setNumberOfUsers(true, "78");

		page.addToCart();

		CartViewPage cart = page.viewCart();
		assertTrue(cart.getStoreSection(STORE_NAME).isResourceInCart(SUBSCRIPTION_ITEM));

		browseCataloguePage = new ShopPage(context).load().pickStoreSingleCatalogue(STORE_NAME);
		browseCataloguePage.setQuery(SUBSCRIPTION_ITEM);
		page = browseCataloguePage.search().getResult(1).viewSummary();

		page.removeFromCart();
		// default to the last number entered - 78
		assertEquals(page.getNumberOfUsers(true), 78);

		browseCataloguePage = new ShopPage(context).load().pickStoreSingleCatalogue(STORE_NAME);
		browseCataloguePage.setQuery(OUTRIGHT_ITEM);
		page = browseCataloguePage.search().getResult(1).viewSummary();

		assertEquals(page.getNumberOfUsers(false), 78);

		page.setNumberOfUsers(false, "80");
		page.addToCart();

		browseCataloguePage = new ShopPage(context).load().pickStoreSingleCatalogue(STORE_NAME);
		browseCataloguePage.setQuery(SUBSCRIPTION_ITEM);
		page = browseCataloguePage.search().getResult(1).viewSummary();

		assertEquals(page.getNumberOfUsers(true), 80);
		page.setNumberOfUsers(true, "90");
		page.addToCart();

		browseCataloguePage = new ShopPage(context).load().pickStoreSingleCatalogue(STORE_NAME);
		browseCataloguePage.setQuery(OUTRIGHT_ITEM);
		page = browseCataloguePage.search().getResult(1).viewSummary();

		assertEquals(page.getNumberOfUsers(false), 80);

		cart = page.viewCart();
		cart.removeAll();

		logout();

	}

	@Override
	protected void cleanupAfterClass() throws Exception
	{
		logon("NumberUserDefault", "``````");

		ShopPage shopPage = new ShopPage(context).load();
		if( shopPage.hasCheckout() )
		{
			CartViewPage viewCart = shopPage.viewCart();
			if( viewCart.doesStoreCartExist(STORE_NAME) )
			{
				viewCart.getStoreSection(STORE_NAME).removeAll();
			}
		}

		super.cleanupAfterClass();
	}
}
