package com.tle.webtests.test.payment.storefront;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.NotPrefixedName;
import com.tle.webtests.pageobject.PrefixedName;
import com.tle.webtests.pageobject.payment.storefront.BrowseCataloguePage;
import com.tle.webtests.pageobject.payment.storefront.CartViewPage;
import com.tle.webtests.pageobject.payment.storefront.CartViewPage.CartStorePart;
import com.tle.webtests.pageobject.payment.storefront.CatalogueResourcePage;
import com.tle.webtests.pageobject.payment.storefront.ShopPage;
import com.tle.webtests.test.AbstractCleanupTest;
import com.tle.webtests.test.payment.global.RegisterStoreAndStoreFront;

/**
 * @see DTEC: #017975
 * @author Seb
 */

@TestInstitution("storefront")
public class RemoveAllCartTest extends AbstractCleanupTest
{
	private static final String STORE_NAME = RegisterStoreAndStoreFront.STORE_NAME;
	private static final String ITEM_1 = "Purchase Outright Item";
	private static final String ITEM_2 = "Subscription Item";
	private static final String ITEM_3 = "Free item";
	private static final PrefixedName CATALOGUE = new NotPrefixedName("cat1");

	@Test
	public void testRemoveAll()
	{
		logon("RemoveAllUser", "``````");

		BrowseCataloguePage catSearchPage = new ShopPage(context).load()
			.pickStoreSingleCatalogue(STORE_NAME, CATALOGUE);

		// add 3 items free, sub, outright
		CatalogueResourcePage page = catSearchPage.exactQuery(ITEM_1).getResult(1).viewSummary();
		page.addToCart();
		catSearchPage = page.clickBreadcrumb(CATALOGUE.toString(), catSearchPage);
		page = catSearchPage.exactQuery(ITEM_2).getResult(1).viewSummary();
		page.addToCart();
		catSearchPage = page.clickBreadcrumb(CATALOGUE.toString(), catSearchPage);
		page = catSearchPage.exactQuery(ITEM_3).getResult(1).viewSummary();
		page.addToCart();

		CartViewPage cart = page.viewCart();
		CartStorePart store = cart.getStoreSection(STORE_NAME);

		Assert.assertTrue(store.isResourceInCart(ITEM_1));
		Assert.assertTrue(store.isResourceInCart(ITEM_2));
		Assert.assertTrue(store.isResourceInCart(ITEM_3));

		store.removeAllCancel();

		Assert.assertTrue(store.isResourceInCart(ITEM_1));
		Assert.assertTrue(store.isResourceInCart(ITEM_2));
		Assert.assertTrue(store.isResourceInCart(ITEM_3));

		store.removeAll();

		Assert.assertFalse(cart.doesStoreCartExist(STORE_NAME));
	}
}
