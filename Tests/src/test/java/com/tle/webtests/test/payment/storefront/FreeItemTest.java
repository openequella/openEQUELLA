package com.tle.webtests.test.payment.storefront;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.payment.storefront.BrowseCataloguePage;
import com.tle.webtests.pageobject.payment.storefront.CartViewPage;
import com.tle.webtests.pageobject.payment.storefront.CartViewPage.CartStorePart;
import com.tle.webtests.pageobject.payment.storefront.CatalogueResourcePage;
import com.tle.webtests.pageobject.payment.storefront.ShopPage;
import com.tle.webtests.test.AbstractCleanupTest;
import com.tle.webtests.test.payment.global.RegisterStoreAndStoreFront;

/**
 * @see DTEC: #017999, #018000
 * @author Seb
 */

@TestInstitution("storefront")
public class FreeItemTest extends AbstractCleanupTest
{
	private static final String STORE_NAME = RegisterStoreAndStoreFront.STORE_NAME;

	@Test
	public void addFreeItemToCart()
	{
		logon("FreeItemTestUser", "``````");

		BrowseCataloguePage catPage = new ShopPage(context).load().pickStoreSingleCatalogue(STORE_NAME);
		CatalogueResourcePage resourcePage = catPage.search("Free item").getResult(1).viewSummary();
		resourcePage.addToCart();

		CartViewPage cartView = resourcePage.viewCart();
		CartStorePart store = cartView.getStoreSection(STORE_NAME);

		Assert.assertEquals(cartView.getBottomTotal(), "Free");
		Assert.assertEquals(cartView.getTopTotal(), "Free");
		Assert.assertTrue(store.isFreeButtonPresent());

		// TODO: check table entries
		Assert.assertFalse(store.startDateColumnPresent());
		Assert.assertFalse(store.usersColumnPresent());

		cartView.removeAll();

		catPage = new ShopPage(context).load().pickStoreSingleCatalogue(STORE_NAME);
		resourcePage = catPage.exactQuery("Subscription Item").getResult(1).viewSummary();
		resourcePage.setNumberOfUsers(true, "55");
		resourcePage.changeDuration(3);
		resourcePage.addToCart();

		catPage = new ShopPage(context).load().pickStoreSingleCatalogue(STORE_NAME);
		resourcePage = catPage.exactQuery("Purchase Outright Item").getResult(1).viewSummary();
		resourcePage.setNumberOfUsers(false, "432123");
		resourcePage.addToCart();

		// checking defaults from previous two cart entries don't break free
		// items
		catPage = new ShopPage(context).load().pickStoreSingleCatalogue(STORE_NAME);
		resourcePage = catPage.exactQuery("Free item").getResult(1).viewSummary();
		resourcePage.addToCart();

		cartView = resourcePage.viewCart();
		store = cartView.getStoreSection(STORE_NAME);

		Assert.assertNotEquals(cartView.getBottomTotal(), "Free");
		Assert.assertNotEquals(cartView.getTopTotal(), "Free");
		Assert.assertFalse(store.isFreeButtonPresent());

		Assert.assertTrue(store.startDateColumnPresent());
		Assert.assertTrue(store.usersColumnPresent());

		store.removeResourceFromCart("Subscription Item");
		store = cartView.getStoreSection(STORE_NAME);

		store.removeResourceFromCart("Purchase Outright Item");
		Assert.assertEquals(cartView.getBottomTotal(), "Free");
		Assert.assertEquals(cartView.getTopTotal(), "Free");

		store = cartView.getStoreSection(STORE_NAME);
		Assert.assertTrue(store.isFreeButtonPresent());

		// TODO: purchase??
		cartView.removeAll();
	}

	@Override
	protected void cleanupAfterClass() throws Exception
	{
		super.cleanupAfterClass();

		logon("FreeItemTestUser", "``````");
		ShopPage shopPage = new ShopPage(context).load();
		if( shopPage.hasCheckout() )
		{
			shopPage.viewCart().removeAll();
		}
		logout();
	}
}
