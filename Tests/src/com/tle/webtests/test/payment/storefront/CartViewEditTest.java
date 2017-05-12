package com.tle.webtests.test.payment.storefront;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.NotPrefixedName;
import com.tle.webtests.pageobject.PrefixedName;
import com.tle.webtests.pageobject.SettingsPage;
import com.tle.webtests.pageobject.payment.backend.StoreSetupPage;
import com.tle.webtests.pageobject.payment.storefront.BrowseCataloguePage;
import com.tle.webtests.pageobject.payment.storefront.CartViewPage;
import com.tle.webtests.pageobject.payment.storefront.CartViewPage.CartStorePart;
import com.tle.webtests.pageobject.payment.storefront.CatalogueResourcePage;
import com.tle.webtests.pageobject.payment.storefront.ShopPage;
import com.tle.webtests.test.AbstractCleanupTest;
import com.tle.webtests.test.payment.global.RegisterStoreAndStoreFront;

/**
 * @see DTEC: #018017, 018047
 * @author Seb
 */
@TestInstitution("storefront")
public class CartViewEditTest extends AbstractCleanupTest
{
	private static final String STORE_NAME = RegisterStoreAndStoreFront.STORE_NAME;
	private static final String STORE_BREAKABLE_NAME = RegisterStoreAndStoreFront.STORE_BREAKABLE_NAME;
	private static final String STORE_NAME_FS = RegisterStoreAndStoreFront.STORE_NAME2;

	private static final String SUBSCRIPTION_ITEM = "Subscription Item";
	private static final PrefixedName CATALOGUE = new NotPrefixedName("cat1");

	@Test
	public void testCart()
	{

		logon("CartTestUser", "``````");
		BrowseCataloguePage browsePage = new ShopPage(context).load().pickStoreSingleCatalogue(STORE_NAME, CATALOGUE);
		// add free -> check -> add outright -> check -> add sub -> check

		CartViewPage cart = browsePage.search("Free item").getResult(1).viewSummary().addToCart().viewCart();
		CartStorePart store = cart.getStoreSection(STORE_NAME);

		Assert.assertFalse(store.startDateColumnPresent());
		Assert.assertFalse(store.usersColumnPresent());
		Assert.assertFalse(store.priceColumnHasDuration());
		Assert.assertEquals(cart.getBottomTotal(), "Free");

		browsePage = new ShopPage(context).load().pickStoreSingleCatalogue(STORE_NAME, CATALOGUE);
		cart = browsePage.search("Purchase Outright Item").getResult(1).viewSummary().addToCart().viewCart();
		store = cart.getStoreSection(STORE_NAME);

		Assert.assertTrue(store.usersColumnPresent());
		Assert.assertFalse(store.startDateColumnPresent());
		Assert.assertFalse(store.priceColumnHasDuration());
		Assert.assertEquals(cart.getTopTotal(), "$5.00 AUD");

		browsePage = new ShopPage(context).load().pickStoreSingleCatalogue(STORE_NAME, CATALOGUE);
		cart = browsePage.search(SUBSCRIPTION_ITEM).getResult(1).viewSummary().addToCart().viewCart();
		store = cart.getStoreSection(STORE_NAME);

		Assert.assertTrue(store.startDateColumnPresent());
		Assert.assertTrue(store.usersColumnPresent());
		Assert.assertTrue(store.priceColumnHasDuration());
		Assert.assertEquals(cart.getBottomTotal(), "$30.00 AUD");

		// add items from other store
		browsePage = new ShopPage(context).load().pickStoreSingleCatalogue(STORE_NAME_FS, CATALOGUE);
		CatalogueResourcePage summary = browsePage.search("Outright and Subscribe Item").getResult(1).viewSummary();
		summary.setPurchaseModel();
		cart = summary.addToCart().viewCart();

		// check cart(s)
		CartStorePart store1 = cart.getStoreSection(STORE_NAME);
		CartStorePart store2 = cart.getStoreSection(STORE_NAME_FS);
		Assert.assertFalse(store2.usersColumnPresent());
		Assert.assertNotEquals(store1.getTotal(), store2.getTotal());
		Assert.assertTrue(cart.isMultipleCurrencies());
		Assert.assertEquals(store1.getTotal(), "$30.00 AUD");
		Assert.assertEquals(store2.getTotal(), "€5.00 EUR");
		store2.removeAll();
		Assert.assertFalse(cart.doesStoreCartExist(STORE_NAME_FS));

		// test x button
		store1 = cart.getStoreSection(STORE_NAME);
		store1.removeResourceFromCart(SUBSCRIPTION_ITEM);

		store1 = cart.getStoreSection(STORE_NAME);
		Assert.assertFalse(store1.isResourceInCart(SUBSCRIPTION_ITEM));
		Assert.assertEquals(cart.getTopTotal(), "$5.00 AUD");
		Assert.assertFalse(store1.priceColumnHasDuration());
		Assert.assertFalse(store1.startDateColumnPresent());
		Assert.assertTrue(store1.usersColumnPresent());

		// remove all
		cart.removeAll();
		Assert.assertFalse(cart.doesStoreCartExist(STORE_NAME));
	}

	/**
	 * http://dev.equella.com/issues/7396
	 */
	@Test(dependsOnMethods = "testCart")
	public void testBrokenStoreInCart()
	{
		logon("CartTestUser", "``````");
		BrowseCataloguePage browsePage = new ShopPage(context).load().pickStoreSingleCatalogue(STORE_NAME, CATALOGUE);

		CartViewPage cart = browsePage.search("Free item").getResult(1).viewSummary().addToCart().viewCart();
		CartStorePart store = cart.getStoreSection(STORE_NAME);
		Assert.assertTrue(store.isResourceInCart("Free item"), "Free item not in cart");

		// add items from breakable store
		browsePage = new ShopPage(context).load().pickStoreSingleCatalogue(STORE_BREAKABLE_NAME, CATALOGUE);
		CatalogueResourcePage summary = browsePage.search("Sonic").getResult(1).viewSummary();
		cart = summary.selectDuration("6 Months").addToCart().viewCart();
		CartStorePart breakableStore = cart.getStoreSection(STORE_BREAKABLE_NAME);
		Assert.assertTrue(breakableStore.isResourceInCart("Sonic"), "Sonic not in cart");

		// disable breakable store
		PageContext context2 = newContext(RegisterStoreAndStoreFront.INSTITUTION_STORE_BREAKABLE);
		logon(context2, "autotest", "automated");
		StoreSetupPage storeEdit = new SettingsPage(context2).load().storeSetupPage();
		storeEdit.setEnabled(false);
		storeEdit.save();

		// reload cart page
		cart = new ShopPage(context).load().viewCart();

		Assert.assertTrue(cart.doesStoreCartExist(STORE_NAME));
		store = cart.getStoreSection(STORE_NAME);
		store.removeAll();

		Assert.assertFalse(cart.doesStoreCartExist(STORE_NAME));

		// enable breakable
		context2 = newContext(RegisterStoreAndStoreFront.INSTITUTION_STORE_BREAKABLE);
		logon(context2, "autotest", "automated");
		storeEdit = new SettingsPage(context2).load().storeSetupPage();
		storeEdit.setEnabled(true);
		storeEdit.save();

		// Remove cart items
		cart = new ShopPage(context).load().viewCart();
		breakableStore = cart.getStoreSection(STORE_BREAKABLE_NAME);
		breakableStore.removeAll();

		// add from breakable
		browsePage = new ShopPage(context).load().pickStoreSingleCatalogue(STORE_BREAKABLE_NAME, CATALOGUE);

		summary = browsePage.search("Sonic").getResult(1).viewSummary();
		cart = summary.selectDuration("3 Months").addToCart().viewCart();
		breakableStore = cart.getStoreSection(STORE_BREAKABLE_NAME);
		Assert.assertTrue(breakableStore.isResourceInCart("Sonic"), "Sonic not in cart");

		// turn off breakable
		context2 = newContext(RegisterStoreAndStoreFront.INSTITUTION_STORE_BREAKABLE);
		logon(context2, "autotest", "automated");
		storeEdit = new SettingsPage(context2).load().storeSetupPage();
		storeEdit.setEnabled(false);
		storeEdit.save();

		// view cart
		cart = new ShopPage(context).load().viewCart();
		Assert.assertFalse(cart.doesStoreCartExist(STORE_NAME));
		Assert.assertTrue(cart.doesStoreCartExist(STORE_BREAKABLE_NAME));

		// enable breakable
		context2 = newContext(RegisterStoreAndStoreFront.INSTITUTION_STORE_BREAKABLE);
		logon(context2, "autotest", "automated");
		storeEdit = new SettingsPage(context2).load().storeSetupPage();
		storeEdit.setEnabled(true);
		storeEdit.save();

		// view cart
		cart = new ShopPage(context).load().viewCart();
		Assert.assertFalse(cart.doesStoreCartExist(STORE_NAME));
		Assert.assertTrue(cart.doesStoreCartExist(STORE_BREAKABLE_NAME));

		// remove all
		cart.removeAll();

		Assert.assertFalse(cart.doesStoreCartExist(STORE_BREAKABLE_NAME));
	}

	@Override
	protected void cleanupAfterClass() throws Exception
	{
		super.cleanupAfterClass();

		PageContext context2 = newContext(RegisterStoreAndStoreFront.INSTITUTION_STORE_BREAKABLE);
		logon(context2, "autotest", "automated");
		StoreSetupPage store = new SettingsPage(context2).load().storeSetupPage();
		store.setEnabled(true);
		store.save();

		logon("CartTestUser", "``````");
		ShopPage shop = new ShopPage(context).load();
		if( shop.hasCheckout() )
		{
			CartViewPage cart = shop.viewCart();
			cart.removeAll();
		}
		logout();
	}
}
