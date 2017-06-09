package com.tle.webtests.test.payment.storefront;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Calendar;
import java.util.Date;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
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
 * @see DTEC: #018004, #018005, #018006, #018009, #018026
 * @author Owner
 */

@TestInstitution("storefront")
public class PurchaseAndSubscribeItemTest extends AbstractCleanupTest
{
	private static final String STORE_NAME = RegisterStoreAndStoreFront.STORE_NAME;
	private static final String STORE_NAME_FR = RegisterStoreAndStoreFront.STORE_NAME2;

	private static final String FIRST_EXPECTED_PRICE = "$5.00 AUD";
	private static final String SECOND_EXPECTED_PRICE = "$385.00 AUD";
	private static final String THIRD_EXPECTED_PRICE = "$38,885.00 AUD";
	private static final String FOURTH_EXPECTED_PRICE = "$25.00 AUD";
	private static final String FITH_EXPECTED_PRICE = "$1,925.00 AUD";
	private static final String SIXTH_EXPECTED_PRICE = "$7,700.00 AUD";
	private static final String SEVENTH_EXPECTED_PRICE = "$777,700.00 AUD";
	private static final String EIGTH_EXPECTED_PRICE = "$1,555,400.00 AUD";

	private static final String FIRST_EXPECTED_PRICE_FR = "€25.00 EUR";
	private static final String SECOND_EXPECTED_PRICE_FR = "€100.00 EUR";
	private static final String THIRD_EXPECTED_PRICE_FR = "€150.00 EUR";
	private static final String FOURTH_EXPECTED_PRICE_FR = "€50.00 EUR";
	private static final String FITH_EXPECTED_PRICE_FR = "€5.00 EUR";

	@AfterMethod
	public void removeCartItems()
	{
		logon("PurchAndSubUser", "``````");
		ShopPage load = new ShopPage(context).load();
		if( load.hasCheckout() )
		{
			CartViewPage viewCart = load.viewCart();
			if( viewCart.doesStoreCartExist(STORE_NAME) )
			{
				CartStorePart storePart = viewCart.getStoreSection(STORE_NAME);
				if( storePart.isResourceInCart("Outright and Subscribe Item") )
				{
					storePart.removeResourceFromCart("Outright and Subscribe Item");
				}
				if( storePart.isResourceInCart("Subscription Item") )
				{
					storePart.removeResourceFromCart("Subscription Item");
				}
			}
		}
	}

	@Test
	public void testItemDetails()
	{
		logon("PurchAndSubUser", "``````");

		BrowseCataloguePage browseCataloguePage = new ShopPage(context).load().pickStoreSingleCatalogue(STORE_NAME);
		browseCataloguePage.setQuery("Outright and Subscribe Item");
		CatalogueResourcePage page = browseCataloguePage.search().getResult(1).viewSummary();
		assertTrue(page.isSubandOutright());
		// First purchase -> check defaults
		assertTrue(page.isPurchaseSelected());
		assertEquals(page.getNumberOfUsers(!page.isPurchaseSelected()), 1);

		page.setSubscriptionModel();

		// TODO: check selected duration is lowest
		assertEquals(page.getNumberOfUsers(!page.isPurchaseSelected()), 1);

		page.setPurchaseModel();

		assertTrue(page.waitForTotal(FIRST_EXPECTED_PRICE));
		page.setNumberOfUsers(false, "77");
		assertTrue(page.waitForTotal(SECOND_EXPECTED_PRICE));
		page.setNumberOfUsers(false, "7777");
		assertTrue(page.waitForTotal(THIRD_EXPECTED_PRICE));
		page.setNumberOfUsers(false, "asddgff++=_121");
		assertTrue(page.waitForTotal("N/A"));

		page.setSubscriptionModel();
		assertTrue(page.waitForTotal(FOURTH_EXPECTED_PRICE));
		// change users -> duration -> users -> duration -> non-number users
		page.setNumberOfUsers(true, "77");
		assertTrue(page.waitForTotal(FITH_EXPECTED_PRICE));
		page.changeDuration(3);
		assertTrue(page.waitForTotal(SIXTH_EXPECTED_PRICE));
		page.setNumberOfUsers(true, "7777");
		assertTrue(page.waitForTotal(SEVENTH_EXPECTED_PRICE));
		page.changeDuration(5);
		assertTrue(page.waitForTotal(EIGTH_EXPECTED_PRICE));
		page.setNumberOfUsers(true, "junk");
		assertTrue(page.waitForTotal("N/A"));

	}

	@Test
	public void testItemDetailsFlatRate()
	{
		logon("PurchAndSubUser", "``````");

		CatalogueResourcePage page = new ShopPage(context).load().pickStoreSingleCatalogue(STORE_NAME_FR)
			.search("Outright and Subscribe Item").getResult(1).viewSummary();
		assertTrue(page.isSubandOutright());
		// check defaults
		assertTrue(page.isPurchaseSelected());
		page.setSubscriptionModel();
		page.selectOtherDate();
		assertFalse(page.isOtherDateDisabled());
		// price changes
		assertEquals(page.getTotal(), FIRST_EXPECTED_PRICE_FR);
		page.changeDuration(3);
		assertEquals(page.getTotal(), SECOND_EXPECTED_PRICE_FR);
		page.changeDuration(4);
		assertEquals(page.getTotal(), THIRD_EXPECTED_PRICE_FR);
		page.changeDuration(2);
		assertEquals(page.getTotal(), FOURTH_EXPECTED_PRICE_FR);
		page.setPurchaseModel();
		assertEquals(page.getTotal(), FITH_EXPECTED_PRICE_FR);
		// check error messages
		assertTrue(page.isOtherDateDisabled());
		page.setSubscriptionModel();
		page.addToCartWithErrors();
		assertTrue(page.isOtherDateInvalid());
		// add 2 cart check
		page.setPurchaseModel();
		page.addToCart();
		assertTrue(page.isMultiTieredDisabled(false));
		page.viewCart().removeAll();
	}

	@Test
	public void itemDefaultsTest()
	{
		/*
		 * Add a sub item to cart -> check that defaults are same -> add
		 * outright purchase to cart -> check new defaults
		 */

		logon("PurchAndSubUser", "``````");

		BrowseCataloguePage browseCataloguePage = new ShopPage(context).load().pickStoreSingleCatalogue(STORE_NAME);
		CatalogueResourcePage page = browseCataloguePage.search("Subscription Item").getResult(1).viewSummary();

		page.setNumberOfUsers(true, "55");
		page.changeDuration(3);
		page.selectOtherDate();
		Calendar otherDate = Calendar.getInstance();
		// 2 months in the future
		otherDate.add(Calendar.MONTH, 2);
		page.setOtherDate(otherDate);
		Date hiddenDateValue = page.getOtherDate(); // this aint right
		page.addToCart();

		browseCataloguePage = new ShopPage(context).load().pickStoreSingleCatalogue(STORE_NAME);
		page = browseCataloguePage.search("Outright and Subscribe Item").getResult(1).viewSummary();

		Assert.assertTrue(page.isSubandOutright());
		Assert.assertFalse(page.isPurchaseSelected());
		// should be 3rd pricing tier
		Assert.assertEquals(page.getSelectedDurationPrice(), "$100.00 AUD");
		Assert.assertFalse(page.isPaymentDateSelected());
		Assert.assertEquals(page.getOtherDate(), hiddenDateValue);
		Assert.assertEquals(page.getNumberOfUsers(true), 55);

		browseCataloguePage = new ShopPage(context).load().pickStoreSingleCatalogue(STORE_NAME);
		page = browseCataloguePage.search("Purchase Outright Item").getResult(1).viewSummary();

		page.setNumberOfUsers(false, "34");
		page.addToCart();

		browseCataloguePage = new ShopPage(context).load().pickStoreSingleCatalogue(STORE_NAME);
		page = browseCataloguePage.search("Outright and Subscribe Item").getResult(1).viewSummary();

		Assert.assertTrue(page.isPurchaseSelected());
		Assert.assertEquals(page.getNumberOfUsers(false), 34);

		page.viewCart().removeAll();
		logout();
	}

	@Test
	public void checkErrors()
	{
		logon("PurchAndSubUser", "``````");

		BrowseCataloguePage browseCataloguePage = new ShopPage(context).load().pickStoreSingleCatalogue(STORE_NAME);
		CatalogueResourcePage page = browseCataloguePage.search("Outright and Subscribe Item").getResult(1)
			.viewSummary();
		assertTrue(page.isSubandOutright());
		assertTrue(page.isPurchaseSelected());
		page.setNumberOfUsers(false, "-15");
		page.addToCartWithErrors();

		Assert.assertTrue(page.isNumberOfUsersInvalid(false));

		page.setNumberOfUsers(false, "×¹²³¼½¾ƒ¬");
		page.addToCartWithErrors();

		assertTrue(page.isNumberOfUsersInvalid(false));

		page.setNumberOfUsers(false, "12");
		page.addToCart();

		assertFalse(page.isNumberOfUsersInvalid(false));

		assertTrue(page.isMultiTieredDisabled(true));

		page.removeFromCart();
		page.setSubscriptionModel();
		page.selectOtherDate();
		page.addToCartWithErrors();

		assertTrue(page.isOtherDateInvalid());

		page.setNumberOfUsers(true, "´¸ªº†‡ÀÁ");
		page.addToCartWithErrors();

		assertTrue(page.isOtherDateInvalid() && page.isNumberOfUsersInvalid(true));

		page.selectPaymentDate();
		page.setNumberOfUsers(true, "24");
		page.addToCart();

		assertFalse(page.isOtherDateInvalid() && page.isNumberOfUsersInvalid(true));

		page.removeFromCart();

	}

}
