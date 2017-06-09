package com.tle.webtests.test.payment.storefront;

import java.util.Calendar;
import java.util.Date;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.NotPrefixedName;
import com.tle.webtests.pageobject.payment.storefront.BrowseCataloguePage;
import com.tle.webtests.pageobject.payment.storefront.CartViewPage;
import com.tle.webtests.pageobject.payment.storefront.CatalogueResourcePage;
import com.tle.webtests.pageobject.payment.storefront.ShopPage;
import com.tle.webtests.test.AbstractCleanupTest;
import com.tle.webtests.test.payment.global.RegisterStoreAndStoreFront;

/**
 * @see DTEC: #017993, #017981, #017992, #017989, #017988
 * @author Seb
 */

@TestInstitution("storefront")
public class SubscriptionItemTest extends AbstractCleanupTest
{
	private static final String STORE_NAME = RegisterStoreAndStoreFront.STORE_NAME;
	private static final String STORE_NAME_FR = RegisterStoreAndStoreFront.STORE_NAME2;

	@Test
	public void checkItemValidation()
	{
		logon("SubscriptionTestUser", "``````");
		CatalogueResourcePage page = new ShopPage(context).load().pickStoreSingleCatalogue(STORE_NAME)
			.search("Subscription Item").getResult(1).viewSummary();

		page.setNumberOfUsers(true, "not a number!");
		page.addToCartWithErrors();

		Assert.assertTrue(page.isNumberOfUsersInvalid(true));

		page.setNumberOfUsers(true, "22");
		page.selectOtherDate();
		page.addToCartWithErrors();

		Assert.assertFalse(page.isNumberOfUsersInvalid(true));
		Assert.assertTrue(page.isOtherDateInvalid());

		page.setNumberOfUsers(true, "15.3");
		page.selectPaymentDate();

		page.addToCartWithErrors();

		Assert.assertTrue(page.isNumberOfUsersInvalid(true));
		Assert.assertFalse(page.isOtherDateInvalid());

		page.setNumberOfUsers(true, "5");

		page.addToCart();

		Assert.assertFalse(page.isNumberOfUsersInvalid(true));

		// Flat rate item
		page = new ShopPage(context).load().pickStoreSingleCatalogue(STORE_NAME_FR, new NotPrefixedName("cat1"))
			.search("Outright and Subscribe Item").getResult(1).viewSummary();
		page.setSubscriptionModel();
		page.selectOtherDate();
		page.addToCartWithErrors();
		Assert.assertTrue(page.isOtherDateInvalid());
		page.selectPaymentDate();
		page.addToCart();

		page.viewCart().removeAll();

		logout(); // resets defaults

	}

	@Test
	public void testCalendarDisabling()
	{
		logon("SubscriptionTestUser", "``````");

		CatalogueResourcePage page = new ShopPage(context).load().pickStoreSingleCatalogue(STORE_NAME)
			.search("Subscription Item").getResult(1).viewSummary();
		// default disabled -> enable -> disable
		Assert.assertTrue(page.isPaymentDateSelected());
		Assert.assertTrue(page.isOtherDateDisabled());
		page.selectOtherDate();
		Assert.assertFalse(page.isPaymentDateSelected());
		Assert.assertFalse(page.isOtherDateDisabled());
		page.selectPaymentDate();
		Assert.assertTrue(page.isPaymentDateSelected());
		Assert.assertTrue(page.isOtherDateDisabled());
		logout();
	}

	@Test
	public void testSubscriptionDefaults()
	{
		final String BREADCRUMB = "cat1";
		logon("SubscriptionTestUser", "``````");

		BrowseCataloguePage browsePage = new ShopPage(context).load().pickStoreSingleCatalogue(STORE_NAME);
		CatalogueResourcePage page = browsePage.search("Subscription Item").getResult(1).viewSummary();

		Assert.assertEquals(page.getNumberOfUsers(true), 1);
		// $25 is the first tier price
		Assert.assertEquals(page.getSelectedDurationPrice(), "$25.00 AUD");
		Assert.assertTrue(page.isPaymentDateSelected());

		browsePage = page.clickBreadcrumb(BREADCRUMB, browsePage);
		page = browsePage.search("Outright and Subscribe Item").getResult(1).viewSummary();

		page.setSubscriptionModel();
		page.changeDuration(4);
		page.selectOtherDate();
		page.setOtherDate(Calendar.getInstance());
		Date otherDate = page.getOtherDate();
		page.setNumberOfUsers(true, "77");
		page.addToCart();

		browsePage = page.clickBreadcrumb(BREADCRUMB, browsePage);
		page = browsePage.search("Subscription Item").getResult(1).viewSummary();

		Assert.assertEquals(page.getNumberOfUsers(true), 77);
		// 4th tier price
		Assert.assertEquals(page.getSelectedDurationPrice(), "$150.00 AUD");
		Assert.assertFalse(page.isPaymentDateSelected());
		Assert.assertEquals(page.getOtherDate(), otherDate);

		page.viewCart().removeAll();

		logout();

	}

	@Override
	protected void cleanupAfterClass() throws Exception
	{
		logon("SubscriptionTestUser", "``````");

		ShopPage shopPage = new ShopPage(context).load();
		if( shopPage.hasCheckout() )
		{
			CartViewPage viewCart = shopPage.viewCart();
			if( viewCart.doesStoreCartExist(STORE_NAME) )
			{
				viewCart.getStoreSection(STORE_NAME).removeAll();
			}
			if( viewCart.doesStoreCartExist(STORE_NAME_FR) )
			{
				viewCart.getStoreSection(STORE_NAME_FR).removeAll();
			}
		}

		super.cleanupAfterClass();
	}
}
