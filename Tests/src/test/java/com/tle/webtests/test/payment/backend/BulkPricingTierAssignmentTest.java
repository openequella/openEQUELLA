package com.tle.webtests.test.payment.backend;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.tle.webtests.framework.Name;
import com.tle.webtests.framework.PageContext;
import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.PrefixedName;
import com.tle.webtests.pageobject.SettingsPage;
import com.tle.webtests.pageobject.payment.backend.CataloguesBulkDialog;
import com.tle.webtests.pageobject.payment.backend.EditCataloguePage;
import com.tle.webtests.pageobject.payment.backend.PricingTiersBulkDialog;
import com.tle.webtests.pageobject.payment.backend.ShowCataloguesPage;
import com.tle.webtests.pageobject.payment.storefront.BrowseCataloguePage;
import com.tle.webtests.pageobject.payment.storefront.CatalogueResourcePage;
import com.tle.webtests.pageobject.payment.storefront.CatalogueSearchList;
import com.tle.webtests.pageobject.payment.storefront.ShopPage;
import com.tle.webtests.pageobject.searching.BulkResultsPage;
import com.tle.webtests.pageobject.searching.ItemAdminPage;
import com.tle.webtests.pageobject.searching.SearchPage;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.test.AbstractCleanupTest;
import com.tle.webtests.test.payment.global.RegisterStoreAndStoreFront;

/**
 * @see DTEC: #017991
 * @author Seb
 */
@TestInstitution("storebackendssl")
public class BulkPricingTierAssignmentTest extends AbstractCleanupTest
{
	private static final String SUBSCRIPTION_SUFFIX = " sub ";
	private static final String PURCHASE_SUFFIX = " pur ";
	private static final String FREE_SUFFIX = " free ";

	private static final String COLLECTION = "Backend Collection";
	private static final String PURCHASE_TIER = "Outright Purchase $5.00 AUD";
	private static final String SUBSCRIPTION_TIER = "Subscription";

	@Name(value = "Catalogue", group = "cats")
	private static PrefixedName CAT_NAME;

	@Name(value = SUBSCRIPTION_SUFFIX, group = "items")
	private static PrefixedName ITEM_NAME_SUB;
	@Name(value = SUBSCRIPTION_SUFFIX + FREE_SUFFIX, group = "items")
	private static PrefixedName ITEM_NAME_SUB_FREE;
	@Name(value = SUBSCRIPTION_SUFFIX + PURCHASE_SUFFIX + FREE_SUFFIX, group = "items")
	private static PrefixedName ITEM_NAME_SUB_PURCHASE_FREE;
	@Name(value = SUBSCRIPTION_SUFFIX + PURCHASE_SUFFIX, group = "items")
	private static PrefixedName ITEM_NAME_SUB_PURCHASE;
	@Name(value = PURCHASE_SUFFIX, group = "items")
	private static PrefixedName ITEM_NAME_PURCHASE;
	@Name(value = PURCHASE_SUFFIX + FREE_SUFFIX, group = "items")
	private static PrefixedName ITEM_NAME_PURCHASE_FREE;
	@Name(value = FREE_SUFFIX, group = "items")
	private static PrefixedName ITEM_NAME_FREE;

	@Test
	public void assignTiersTest()
	{
		boolean bulkSuccessful = false;

		logon("autotest", "automated");

		/*
		 * create 7 items: sub, sub + pur, sub + pur + free, pur, free, pur +
		 * free, sub + free
		 */
		// sub
		WizardPageTab wizard = new ContributePage(context).load().openWizard(COLLECTION);
		wizard.editbox(1, ITEM_NAME_SUB);
		wizard.save().publish();
		// sub + pur
		wizard = new ContributePage(context).load().openWizard(COLLECTION);
		wizard.editbox(1, ITEM_NAME_SUB_PURCHASE);
		wizard.save().publish();
		// sub + pur + free
		wizard = new ContributePage(context).load().openWizard(COLLECTION);
		wizard.editbox(1, ITEM_NAME_SUB_PURCHASE_FREE);
		wizard.save().publish();
		// pur
		wizard = new ContributePage(context).load().openWizard(COLLECTION);
		wizard.editbox(1, ITEM_NAME_PURCHASE);
		wizard.save().publish();
		// free
		wizard = new ContributePage(context).load().openWizard(COLLECTION);
		wizard.editbox(1, ITEM_NAME_FREE);
		wizard.save().publish();
		// pur + free
		wizard = new ContributePage(context).load().openWizard(COLLECTION);
		wizard.editbox(1, ITEM_NAME_PURCHASE_FREE);
		wizard.save().publish();
		// sub + free
		wizard = new ContributePage(context).load().openWizard(COLLECTION);
		wizard.editbox(1, ITEM_NAME_SUB_FREE);
		wizard.save().publish();

		ShowCataloguesPage catPage = new SettingsPage(context).load().showCataloguesPage();
		EditCataloguePage editCat = catPage.createCatalogue();
		editCat.setName(CAT_NAME);
		editCat.setDescription("desc");
		editCat.setEnabled(true);
		catPage = editCat.save();
		Assert.assertTrue(catPage.entityExists(CAT_NAME));

		ItemAdminPage manageResources = new ItemAdminPage(context).load();
		// assign to catalogue
		manageResources.search(prefix());
		manageResources.bulk().selectAll();
		CataloguesBulkDialog catDialog = manageResources.bulk().addCatalogues();

		catDialog.getCatalogueShuffleBox().moveRightByText(CAT_NAME.toString());
		BulkResultsPage bulk = catDialog.executeBulk();
		bulkSuccessful = bulk.waitForAll().noErrors();
		manageResources = bulk.close(manageResources);

		// assign subs
		manageResources.search(ITEM_NAME_SUB.toString());
		manageResources.bulk().selectAll();
		PricingTiersBulkDialog priceDialog = manageResources.bulk().tiers();
		priceDialog.assignSubscriptionTier(SUBSCRIPTION_TIER);
		bulk = priceDialog.executeBulk();
		bulkSuccessful &= bulk.waitForAll().noErrors();
		manageResources = bulk.close(manageResources);

		// assign outright purchase
		manageResources.search(ITEM_NAME_PURCHASE.toString());
		manageResources.bulk().selectAll();
		priceDialog = manageResources.bulk().tiers();
		priceDialog.assignOutrightTier(PURCHASE_TIER);
		bulk = priceDialog.executeBulk();
		bulkSuccessful &= bulk.waitForAll().noErrors();
		manageResources = bulk.close(manageResources);

		// assign free
		manageResources.search(ITEM_NAME_FREE.toString());
		manageResources.bulk().selectAll();
		priceDialog = manageResources.bulk().tiers();
		priceDialog.assignFree();
		bulk = priceDialog.executeBulk();
		bulkSuccessful &= bulk.waitForAll().noErrors();
		manageResources = bulk.close(manageResources);

		// assign pur + sub
		manageResources.search(ITEM_NAME_SUB_PURCHASE.toString());
		manageResources.bulk().selectAll();
		priceDialog = manageResources.bulk().tiers();
		priceDialog.assignOutrightTier(PURCHASE_TIER);
		priceDialog.assignSubscriptionTier(SUBSCRIPTION_TIER);
		bulk = priceDialog.executeBulk();
		bulkSuccessful &= bulk.waitForAll().noErrors();
		manageResources = bulk.close(manageResources);

		// assign free + sub
		manageResources.search(ITEM_NAME_SUB_FREE.toString());
		manageResources.bulk().selectAll();
		priceDialog = manageResources.bulk().tiers();
		priceDialog.assignFree();
		priceDialog.assignSubscriptionTier(SUBSCRIPTION_TIER);
		bulk = priceDialog.executeBulk();
		bulkSuccessful &= bulk.waitForAll().noErrors();
		manageResources = bulk.close(manageResources);

		// assign free + purchase
		manageResources.search(ITEM_NAME_PURCHASE_FREE.toString());
		manageResources.bulk().selectAll();
		priceDialog = manageResources.bulk().tiers();
		priceDialog.assignFree();
		priceDialog.assignOutrightTier(PURCHASE_TIER);
		bulk = priceDialog.executeBulk();
		bulkSuccessful &= bulk.waitForAll().noErrors();
		manageResources = bulk.close(manageResources);

		// assign purchase + sub + free
		manageResources.search(ITEM_NAME_SUB_PURCHASE_FREE.toString());
		manageResources.bulk().selectAll();
		priceDialog = manageResources.bulk().tiers();
		priceDialog.assignFree();
		priceDialog.assignSubscriptionTier(SUBSCRIPTION_TIER);
		priceDialog.assignOutrightTier(PURCHASE_TIER);
		bulk = priceDialog.executeBulk();
		bulkSuccessful &= bulk.waitForAll().noErrors();
		manageResources = bulk.close(manageResources);

		Assert.assertTrue(bulkSuccessful);

		logout();
	}

	@Test(dependsOnMethods = {"assignTiersTest"})
	public void testBackend()
	{
		logon("autotest", "automated");

		SearchPage search = new SearchPage(context).load();
		search.search(prefix());
		search.setSort("name");

		// free
		SummaryPage page = search.results().getResultForTitle(ITEM_NAME_FREE).viewSummary();
		Assert.assertTrue(page.isItemFree());
		// purchase
		search = new SearchPage(context).load();
		page = search.results().getResultForTitle(ITEM_NAME_PURCHASE).viewSummary();
		Assert.assertTrue(page.hasPurchase());
		// pur + free
		search = new SearchPage(context).load();
		page = search.results().getResultForTitle(ITEM_NAME_PURCHASE_FREE).viewSummary();
		Assert.assertTrue(page.isItemFree());
		// sub
		search = new SearchPage(context).load();
		page = search.results().getResultForTitle(ITEM_NAME_SUB).viewSummary();
		Assert.assertTrue(page.hasSubscription());
		// sub + free
		search = new SearchPage(context).load();
		page = search.results().getResultForTitle(ITEM_NAME_SUB_FREE).viewSummary();
		Assert.assertTrue(page.isItemFree());
		// sub + pur
		search = new SearchPage(context).load();
		page = search.results().getResultForTitle(ITEM_NAME_SUB_PURCHASE).viewSummary();
		Assert.assertTrue(page.hasSubscription());
		Assert.assertTrue(page.hasPurchase());
		// sub + pur + free
		search = new SearchPage(context).load();
		page = search.results().getResultForTitle(ITEM_NAME_SUB_PURCHASE_FREE).viewSummary();
		Assert.assertTrue(page.isItemFree());

		logout();
	}

	@Test(dependsOnMethods = {"assignTiersTest"})
	public void testStorefront()
	{
		final String STORE_NAME = RegisterStoreAndStoreFront.STORE_NAME;
		final String SORT = "name";

		PageContext context = newContext("storefront");
		logon(context, "autotest", "automated");

		BrowseCataloguePage browsePage = new ShopPage(context).load().pickStoreSingleCatalogue(STORE_NAME, CAT_NAME);
		browsePage.setSort(SORT);
		CatalogueSearchList resultList = browsePage.search(prefix());
		// free
		CatalogueResourcePage page = resultList.getResultForTitle(ITEM_NAME_FREE).viewSummary();
		Assert.assertTrue(page.isFree());
		browsePage = page.clickBreadcrumb(CAT_NAME, browsePage);
		browsePage.setSort(SORT);
		// purchase
		page = resultList.getResultForTitle(ITEM_NAME_PURCHASE).viewSummary();
		Assert.assertTrue(page.isOutrightPurchase());
		browsePage = page.clickBreadcrumb(CAT_NAME, browsePage);
		browsePage.setSort(SORT);
		// pur + free
		page = resultList.getResultForTitle(ITEM_NAME_PURCHASE_FREE).viewSummary();
		Assert.assertTrue(page.isFree());
		browsePage = page.clickBreadcrumb(CAT_NAME, browsePage);
		browsePage.setSort(SORT);
		// sub
		page = resultList.getResultForTitle(ITEM_NAME_SUB).viewSummary();
		Assert.assertTrue(page.isSubscription());
		browsePage = page.clickBreadcrumb(CAT_NAME, browsePage);
		browsePage.setSort(SORT);
		// sub + free
		page = resultList.getResultForTitle(ITEM_NAME_SUB_FREE).viewSummary();
		Assert.assertTrue(page.isFree());
		browsePage = page.clickBreadcrumb(CAT_NAME, browsePage);
		browsePage.setSort(SORT);
		// sub + pur
		page = resultList.getResultForTitle(ITEM_NAME_SUB_PURCHASE).viewSummary();
		Assert.assertTrue(page.isSubandOutright());
		browsePage = page.clickBreadcrumb(CAT_NAME, browsePage);
		browsePage.setSort(SORT);
		// sub + pur + free
		page = resultList.getResultForTitle(ITEM_NAME_SUB_PURCHASE_FREE).viewSummary();
		Assert.assertTrue(page.isFree());
	}

	@Override
	protected void cleanupAfterClass() throws Exception
	{
		super.cleanupAfterClass();
		ShowCataloguesPage catPage = new SettingsPage(context).load().showCataloguesPage();
		catPage.deleteAllNamed(getNames("cats"));
		Assert.assertFalse(catPage.entityExists(CAT_NAME), CAT_NAME + " catalogue never got deleleted");
	}
}
