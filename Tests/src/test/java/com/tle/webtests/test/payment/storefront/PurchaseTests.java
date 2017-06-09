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
import com.tle.webtests.pageobject.payment.storefront.CatalogueSearchList;
import com.tle.webtests.pageobject.payment.storefront.CatalogueSearchResult;
import com.tle.webtests.pageobject.payment.storefront.ShopPage;
import com.tle.webtests.test.AbstractSessionTest;
import com.tle.webtests.test.payment.global.RegisterStoreAndStoreFront;

/**
 * @author Aaron
 */
@TestInstitution("storefront")
public class PurchaseTests extends AbstractSessionTest
{
	private static final String NO_NAME_ITEM = "e93e2082-4483-4684-9072-2287aa6516a0";
	private static final String STORE_NAME = RegisterStoreAndStoreFront.STORE_NAME;
	private static final PrefixedName CATALOGUE = new NotPrefixedName("cat1");

	/**
	 * http://dev.equella.com/issues/7416
	 */
	@Test
	public void testNoNameItem()
	{
		logon("PurchAndSubUser", "``````");

		BrowseCataloguePage browseCataloguePage = new ShopPage(context).load().pickStoreSingleCatalogue(STORE_NAME,
			CATALOGUE);
		CatalogueSearchList results = browseCataloguePage.setQuery("No name item").search();
		CatalogueSearchResult resultForTitle = results.getResultForTitle(NO_NAME_ITEM, 1);
		CatalogueResourcePage viewSummary = resultForTitle.viewSummary();
		CatalogueResourcePage addToCart = viewSummary.setPurchaseModel().addToCart();
		CartViewPage cart = addToCart.viewCart();
		CartStorePart store = cart.getStoreSection(STORE_NAME);

		Assert.assertTrue(store.isResourceInCart(NO_NAME_ITEM), "No-name item not in cart");

		store.removeResourceFromCart(NO_NAME_ITEM);
		Assert.assertTrue(
			!cart.doesStoreCartExist(STORE_NAME) || !cart.getStoreSection(STORE_NAME).isResourceInCart(STORE_NAME),
			"No-name item still in cart");
	}
}
