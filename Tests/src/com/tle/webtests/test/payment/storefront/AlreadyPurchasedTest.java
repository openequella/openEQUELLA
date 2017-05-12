package com.tle.webtests.test.payment.storefront;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.Calendar;

import org.testng.annotations.Test;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.myresources.MyResourcesPage;
import com.tle.webtests.pageobject.payment.storefront.BrowseCataloguePage;
import com.tle.webtests.pageobject.payment.storefront.CartViewPage;
import com.tle.webtests.pageobject.payment.storefront.CatalogueResourcePage;
import com.tle.webtests.pageobject.payment.storefront.PurchaseDetailsTable;
import com.tle.webtests.pageobject.payment.storefront.ShopPage;
import com.tle.webtests.pageobject.searching.ItemAdminPage;
import com.tle.webtests.pageobject.searching.ItemListPage;
import com.tle.webtests.pageobject.searching.ItemSearchResult;
import com.tle.webtests.pageobject.searching.SearchPage;
import com.tle.webtests.pageobject.tasklist.NotificationsPage;
import com.tle.webtests.test.AbstractCleanupTest;
import com.tle.webtests.test.payment.global.RegisterStoreAndStoreFront;

/**
 * @see DTEC: #018016, #018028, #018030, #017430
 * @author Seb etc
 */
// Uses pre-bought items, purchasing is covered elsewhere
@TestInstitution("storefront")
public class AlreadyPurchasedTest extends AbstractCleanupTest
{
	private static final String STORE_PER_USER = RegisterStoreAndStoreFront.STORE_NAME;
	private static final String STORE_FLATE_RATE = RegisterStoreAndStoreFront.STORE_NAME2;
	private static final String SUBSCRIBED_ITEM = " Subscribed Item";
	private static final String OUTRIGHT_PURCHASED_ITEM = " Outright Purchased Item";
	private static final String FREE_ITEM = " Free Item";
	private static final String FLAT_RATE_SUFFIX = " flat rate";
	private static final String SUBSCRIBED_MESSAGE = "This resource has the following active subscriptions";
	private static final String PURCHASED_MESSAGE = "This resource has been purchased previously";
	private static final String BASIC_COLLECTION = "Basic Items";
	private static final String BASIC_COLLECTION_UUID = "b28f1ffe-2008-4f5e-d559-83c8acd79316";
	private static final String BACKEND_COLLECTION = "Backend Collection";
	private static final String BACKEND_COLLECTION_UUID = "7d4e9473-5cff-4b43-9322-394bfa36e1eb";
	// the browser default is en-us
	private static final String PURCHASED_FR_MESSAE = "This resource was purchased on Sep 10, 2012";
	private static final String PURCHASED_TAB = "purchased";
	private static final String BREADCRUMB = "cat1";

	@Test
	public void checkPurchasedItems()
	{
		logon("AlreadyPurchasedUser", "``````");

		BrowseCataloguePage browsePage = new ShopPage(context).load().pickStoreSingleCatalogue(STORE_PER_USER);
		CatalogueResourcePage page = browsePage.search(namePrefix + SUBSCRIBED_ITEM).getResult(1).viewSummary();

		assertTrue(page.isResourcePurchased());
		assertEquals(page.getPurchasedMessage(), SUBSCRIBED_MESSAGE);
		assertTrue(page.isAddToCartButtonPresent());
		assertTrue(page.isPreviousPurchasersTablePresent());
		// TODO: check purchased details

		browsePage = page.clickBreadcrumb(BREADCRUMB, browsePage);
		page = browsePage.search(namePrefix + OUTRIGHT_PURCHASED_ITEM).getResult(1).viewSummary();

		assertTrue(page.isResourcePurchased());
		assertEquals(page.getPurchasedMessage(), PURCHASED_MESSAGE);
		assertTrue(page.isAddToCartButtonPresent());
		assertTrue(page.isPreviousPurchasersTablePresent());
		// TODO: check purchased details

		browsePage = new ShopPage(context).load().pickStoreSingleCatalogue(STORE_FLATE_RATE);
		page = browsePage.search(namePrefix + SUBSCRIBED_ITEM + FLAT_RATE_SUFFIX).getResult(1).viewSummary();

		assertTrue(page.isResourcePurchased());
		assertEquals(page.getPurchasedMessage(), SUBSCRIBED_MESSAGE);
		assertTrue(page.isAddToCartButtonPresent());
		assertTrue(page.isPreviousPurchasersTablePresent());
		// TODO: check purchased details

		browsePage = page.clickBreadcrumb(BREADCRUMB, browsePage);
		page = browsePage.search(namePrefix + OUTRIGHT_PURCHASED_ITEM + FLAT_RATE_SUFFIX).getResult(1).viewSummary();

		assertTrue(page.isResourcePurchased());
		assertEquals(page.getPurchasedMessage(), PURCHASED_FR_MESSAE);
		assertFalse(page.isAddToCartButtonPresent());
		assertFalse(page.isPreviousPurchasersTablePresent());

		browsePage = page.clickBreadcrumb(BREADCRUMB, browsePage);
		page = browsePage.search(namePrefix + FREE_ITEM + FLAT_RATE_SUFFIX).getResult(1).viewSummary();

		assertTrue(page.isResourcePurchased());
		assertEquals(page.getPurchasedMessage(), PURCHASED_FR_MESSAE);
		assertFalse(page.isAddToCartButtonPresent());
		assertFalse(page.isPreviousPurchasersTablePresent());
	}

	@Test
	public void checkHarvestedItems()
	{
		logon(AUTOTEST_LOGON, AUTOTEST_PASSWD);
		SearchPage search = new SearchPage(context).load();
		search.search(namePrefix);
		search.setSort("name");
		// 1 free flat | 2 outright p/user | 3 outright flat | 4 subscribed flat

		PurchaseDetailsTable purchaseDetails = search.results()
			.getResultForTitle(namePrefix + FREE_ITEM + FLAT_RATE_SUFFIX, 1).viewSummary().getPurchaseDetails();
		assertEquals(purchaseDetails.getBuyer(), "Auto Test");
		assertEquals(purchaseDetails.getPricePaid(), "Free");
		assertEquals(purchaseDetails.getUsers(), "Flat Rate");
		assertFalse(purchaseDetails.startDateExists());

		search.load();
		purchaseDetails = search.results().getResultForTitle(namePrefix + OUTRIGHT_PURCHASED_ITEM).viewSummary()
			.getPurchaseDetails();
		assertEquals(purchaseDetails.getBuyer(), "Auto Test");
		assertTrue(purchaseDetails.getPricePaid().equals("$15.00 AUD"));
		assertEquals(purchaseDetails.getUsers(), "3");
		assertFalse(purchaseDetails.startDateExists());

		search.load();
		purchaseDetails = search.results().getResultForTitle(namePrefix + OUTRIGHT_PURCHASED_ITEM + FLAT_RATE_SUFFIX)
			.viewSummary().getPurchaseDetails();
		assertEquals(purchaseDetails.getBuyer(), "Auto Test");
		assertTrue(purchaseDetails.getPricePaid().equals("€5.00 EUR"));
		assertEquals(purchaseDetails.getUsers(), "Flat Rate");
		assertFalse(purchaseDetails.startDateExists());

		search.load();
		purchaseDetails = search.results().getResultForTitle(namePrefix + SUBSCRIBED_ITEM + FLAT_RATE_SUFFIX)
			.viewSummary().getPurchaseDetails();
		assertEquals(purchaseDetails.getBuyer(), "Auto Test");
		assertEquals(purchaseDetails.getPricePaid(), "€200.00 EUR");
		assertEquals(purchaseDetails.getUsers(), "Flat Rate");
		assertTrue(purchaseDetails.startDateExists());

		// check non-bought item
		search = new SearchPage(context).load();
		search.search("Non bought item in shopfront collection");
		purchaseDetails = search.results().getResult(1).viewSummary().getPurchaseDetails();
		assertNull(purchaseDetails);
	}

	@Test
	public void myResourcesPurchasesTest()
	{
		logon(AUTOTEST_LOGON, AUTOTEST_PASSWD);
		MyResourcesPage purchased = new MyResourcesPage(context, PURCHASED_TAB).load();
		ItemListPage search = purchased.search(namePrefix);
		for( ItemSearchResult item : search.getResults() )
		{
			if( item.getTitle().contains(SUBSCRIBED_ITEM) )
			{
				assertTrue(item.hasResubscribeButton());
			}
			else
			{
				assertFalse(item.hasResubscribeButton());
			}
		}

		// Set sort and filter to ensure correct item
		purchased.setSort("name");
		purchased.setItemStatusFilter("archived");

		CatalogueResourcePage catPage = purchased.results().getResultForTitle(namePrefix + SUBSCRIBED_ITEM)
			.resubscribe();

		// payment date should be selected by default
		assertTrue(catPage.isPaymentDateSelected());

		catPage.addToCart().viewCart().getStoreSection(STORE_FLATE_RATE)
			.removeResourceFromCart(namePrefix + SUBSCRIBED_ITEM + " flat rate");
		logout();
		logon("OrderHistoryPurchaser", "``````"); // user with 0 purchases
		purchased = new MyResourcesPage(context, PURCHASED_TAB).load();
		purchased.search(namePrefix);
		assertFalse(purchased.hasResults());
	}

	@Test
	public void manageResourcesFilterTest()
	{
		final String SUB_ITEM = namePrefix + SUBSCRIBED_ITEM + FLAT_RATE_SUFFIX;
		logon(AUTOTEST_LOGON, AUTOTEST_PASSWD);
		ItemAdminPage manageResources = new ItemAdminPage(context).load();
		int totalResources = manageResources.results().getTotalAvailable();
		manageResources.filterByPurchased();
		assertTrue(manageResources.results().getResults().size() < totalResources);
		for( ItemSearchResult item : manageResources.results().getResults() )
		{
			// only test relevant items
			if( item.getTitle().contains(namePrefix) )
			{
				if( item.getTitle().contains(SUBSCRIBED_ITEM) )
				{
					assertTrue(item.hasResubscribeButton());
				}
				else
				{
					assertFalse(item.hasResubscribeButton());
				}
			}
		}
		// end date filter
		ItemListPage search = manageResources.search(SUB_ITEM).get();
		Calendar c1 = Calendar.getInstance();
		Calendar c2 = Calendar.getInstance();
		// the sub items sub end date is 10/12/2012
		// after
		c1.set(Calendar.YEAR, 2010);
		manageResources.setSubscriptionDateFilter("AFTER", new Calendar[]{c1, c2});
		assertTrue(search.doesResultExist(SUB_ITEM, 1));
		// before
		c1.set(Calendar.YEAR, 2020);
		manageResources.setSubscriptionDateFilter("BEFORE", new Calendar[]{c1, c2});
		assertTrue(search.doesResultExist(SUB_ITEM, 1));
		// between
		c1.set(Calendar.YEAR, 2009);
		c2.set(Calendar.YEAR, 2020);
		manageResources.setSubscriptionDateFilter("BETWEEN", new Calendar[]{c1, c2});
		assertTrue(search.doesResultExist(SUB_ITEM, 1));
		// On
		c1.set(Calendar.YEAR, 2015);
		c1.set(Calendar.MONTH, Calendar.DECEMBER);
		c1.set(Calendar.DATE, 10);
		manageResources.setSubscriptionDateFilter("ON", new Calendar[]{c1, c2});
		assertTrue(search.doesResultExist(SUB_ITEM, 1));

		manageResources.filterByPurchased(); // deselect
		assertTrue(search.doesResultExist(SUB_ITEM, 1));

	}

	/**
	 * For this test we refer to the backend store to verify that users at that
	 * end have (or have not) received notifications of what has transpired at
	 * the storefront end.
	 */
	@Test
	public void testNotifications()
	{
		PageContext backendContext = newContext("storebackend2ssl");
		logon(backendContext, "storeuser", "equella");
		NotificationsPage notifications = new NotificationsPage(backendContext).load();
		notifications.setReasonFilter("itemsale");
		assertEquals(notifications.results().getResults().size(), 0, "sale notifications found for wrong user");
		logout(backendContext);

		logon(backendContext, AUTOTEST_LOGON, AUTOTEST_PASSWD);
		notifications = new NotificationsPage(backendContext).load();
		notifications.setReasonFilter("itemsale");

		assertTrue(notifications.results().doesResultExist(namePrefix + SUBSCRIBED_ITEM),
			"Subscripion notification missing");
		assertTrue(notifications.results().doesResultExist(namePrefix + OUTRIGHT_PURCHASED_ITEM),
			"Outright purchase notification missing");
		assertTrue(notifications.results().doesResultExist(namePrefix + FREE_ITEM), "Free Item notification missing");

		logout(backendContext);
		logon(backendContext, AUTOTEST_LOGON, AUTOTEST_PASSWD);
		notifications = new NotificationsPage(backendContext).load();
		notifications.setCollectionFilter(BASIC_COLLECTION_UUID);
		notifications.search(namePrefix);
		assertEquals(notifications.results().getResults().size(), 0, "sale notifications found for wrong collection ("
			+ BASIC_COLLECTION + ": expected only " + BACKEND_COLLECTION + ')');
		logout(backendContext);
		logon(backendContext, AUTOTEST_LOGON, AUTOTEST_PASSWD);
		notifications = new NotificationsPage(backendContext).load();
		notifications.setCollectionFilter(BACKEND_COLLECTION_UUID);
		notifications.search(namePrefix);
		assertTrue(notifications.hasResults(), "Expected some (in fact, at least 3) for query on " + namePrefix);
		assertTrue(notifications.results().doesResultExist(namePrefix + SUBSCRIBED_ITEM),
			"Subscripion notification missing");
		assertTrue(notifications.results().doesResultExist(namePrefix + OUTRIGHT_PURCHASED_ITEM),
			"Outright purchase notification missing");
		assertTrue(notifications.results().doesResultExist(namePrefix + FREE_ITEM), "Free Item notification missing");
		logout(backendContext);
	}

	@Override
	protected void cleanupAfterClass() throws Exception
	{
		logon(AUTOTEST_LOGON, AUTOTEST_PASSWD);
		ShopPage shop = new ShopPage(context).load();
		if( shop.hasCheckout() )
		{
			CartViewPage viewCart = shop.viewCart();
			if( viewCart.doesStoreCartExist(STORE_PER_USER) )
			{
				viewCart.getStoreSection(STORE_PER_USER).removeAll();
			}

			if( viewCart.doesStoreCartExist(STORE_FLATE_RATE) )
			{
				viewCart.getStoreSection(STORE_FLATE_RATE).removeAll();
			}
		}
	}
}
