package com.tle.webtests.test.payment.backend;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.NotPrefixedName;
import com.tle.webtests.pageobject.PrefixedName;
import com.tle.webtests.pageobject.payment.storefront.CartViewPage;
import com.tle.webtests.pageobject.payment.storefront.CatalogueResourcePage;
import com.tle.webtests.pageobject.payment.storefront.OrderPage;
import com.tle.webtests.pageobject.payment.storefront.ShopPage;
import com.tle.webtests.pageobject.searching.ItemListPage;
import com.tle.webtests.pageobject.searching.SearchPage;
import com.tle.webtests.pageobject.settings.ScheduledTasksPage;
import com.tle.webtests.pageobject.tasklist.NotificationSearchResults;
import com.tle.webtests.pageobject.tasklist.NotificationsPage;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.test.AbstractCleanupAutoTest;
import com.tle.webtests.test.payment.global.RegisterStoreAndStoreFront;

/**
 * @author Seb
 */

@TestInstitution("storebackendssl")
public class ItemSaleNotificationTest extends AbstractCleanupAutoTest
{
	// Create item -> buy item -> run task -> check notification -> edit item ->
	// run task -> check storefront notification
	private static final String STORE_NAME = RegisterStoreAndStoreFront.STORE_NAME;
	private static final String PURCHASE_TIER = "Outright Purchase $5.00 AUD";
	private static final PrefixedName CATALOGUE = new NotPrefixedName("cat1");

	@Test
	public void setupItem()
	{
		logon("autotest", "automated");
		WizardPageTab wizard = new ContributePage(context).load().openWizard("Backend Collection");
		wizard.editbox(1, namePrefix + " item");
		SummaryPage summary = wizard.save().publish();

		summary.editPricingTiersPage().selectOutrightPricingTier(PURCHASE_TIER).save();
		summary.editCataloguesPage().addToWhiteList(CATALOGUE);
		logout();
	}

	@Test(dependsOnMethods = {"setupItem"})
	public void buyItem()
	{
		PageContext context = newContext("storefront");

		logon(context, "SaleNotificationBuyer", "``````");
		CatalogueResourcePage resource = new ShopPage(context).load().pickStoreSingleCatalogue(STORE_NAME, CATALOGUE)
			.search(namePrefix).getResult(1).viewSummary();
		resource.setNumberOfUsers(false, "10");
		assertEquals(resource.getTotal(), "$50.00 AUD");
		resource.addToCart();
		CartViewPage cart = resource.viewCart();
		OrderPage orderPage = cart.getStoreSection(STORE_NAME).payWithDemoGateway();
		orderPage.waitForStatus("Paid");
		String status = orderPage.getOrderStatus();
		assertEquals(status, "Paid");

		final SearchPage searchPage = new SearchPage(context).load();
		searchPage.search(namePrefix);
		ItemListPage results = searchPage.waitForResult(namePrefix + " item", 1);
		assertTrue(results.doesResultExist(namePrefix + " item", 1));

		final NotificationsPage notification = new NotificationsPage(context).load();
		notification.setReasonFilter("piupdate");
		NotificationSearchResults notifications = notification.search(namePrefix + " item");
		assertTrue(notifications.doesResultExist(namePrefix + " item", 1));
		notification.results().clearNotification(namePrefix + " item", 1);

	}

	private void runScheduledTasks(PageContext context)
	{
		logon("TLE_ADMINISTRATOR", "tle010", context);
		new ScheduledTasksPage(context).load().runCheckUpdatedPurchasedItemsTask(true);
		logout();

	}

	@Test(dependsOnMethods = {"buyItem"})
	public void checkNotification()
	{
		logon("autotest", "automated");
		NotificationsPage notifications = new NotificationsPage(context).load();
		notifications.setReasonFilter("itemsale");
		notifications.search(namePrefix);

		Assert.assertTrue(notifications.results().doesResultExist(namePrefix + " item"));
		// TODO: check items search result accuracy (pricing tiers, reason)
		notifications.results().clearNotification(namePrefix + " item", 1);
		Assert.assertFalse(notifications.results().doesResultExist(namePrefix + " item"));
	}

	@Test(dependsOnMethods = {"buyItem"})
	public void checkPurchaseUpdatedNotification()
	{
		final String ITEM_NAME = namePrefix + " item";
		final String DESC = "new description heyoo";

		logon("autotest", "automated");
		SummaryPage summary = new SearchPage(context).load().search(namePrefix).getResult(1).viewSummary();
		WizardPageTab wiz = summary.newVersion();
		wiz.editbox(2, DESC);
		wiz.save().publish();
		logout();

		PageContext context = newContext("storefront");

		runScheduledTasks(context);
		logon(context, "SaleNotificationBuyer", "``````");
		final NotificationsPage notifications = new NotificationsPage(context).load();
		notifications.setReasonFilter("piupdate");
		notifications.search(namePrefix);
		NotificationSearchResults results = notifications.waitForResult(ITEM_NAME, 1);

		SummaryPage updatedItem = results.getResultForTitle(ITEM_NAME, 1).viewSummary();
		Assert.assertEquals(updatedItem.getItemDescription(), DESC);

		notifications.load();
		notifications.search(ITEM_NAME).clearNotification(ITEM_NAME, 1);
		Assert.assertFalse(notifications.results().doesResultExist(ITEM_NAME));

	}

	@Override
	protected void cleanupAfterClass() throws Exception
	{

	}
}
