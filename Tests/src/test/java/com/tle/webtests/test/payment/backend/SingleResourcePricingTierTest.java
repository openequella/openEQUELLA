package com.tle.webtests.test.payment.backend;

import static org.testng.Assert.assertTrue;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.NotPrefixedName;
import com.tle.webtests.pageobject.PrefixedName;
import com.tle.webtests.pageobject.payment.backend.ItemPricingTiersPage;
import com.tle.webtests.pageobject.payment.backend.ItemPricingTiersPage.SubscriptionTierRow;
import com.tle.webtests.pageobject.payment.storefront.BrowseCataloguePage;
import com.tle.webtests.pageobject.payment.storefront.CatalogueResourcePage;
import com.tle.webtests.pageobject.payment.storefront.ShopPage;
import com.tle.webtests.pageobject.searching.SearchPage;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.test.AbstractCleanupTest;
import com.tle.webtests.test.payment.global.RegisterStoreAndStoreFront;

/**
 * @see DTEC: #017990
 * @author Seb
 */
@TestInstitution("storebackendssl")
public class SingleResourcePricingTierTest extends AbstractCleanupTest
{
	private static final PrefixedName CATALOGUE = new NotPrefixedName("cat1");
	private static final String COLLECTION = "Backend Collection";
	private static final String OUTRIGHT_PRICING_TIER = "Outright Purchase $5.00 AUD";
	private static final String STORE_NAME = RegisterStoreAndStoreFront.STORE_NAME;
	private static final String ITEM_SUFFIX = " pricing tiers item";
	private static final String SUBSCRIPTION_TIER = "Subscription";
	private String weekPrice;
	private String monthPrice;
	private String yearPrice;

	public SingleResourcePricingTierTest()
	{
		setDeleteCredentials("AutoTest", "automated");
	}

	@Test
	public void setupItemTest()
	{
		logon("autotest", "automated");

		// contribute item with namePrefix + pricing tier w/e
		WizardPageTab wizard = new ContributePage(context).load().openWizard(COLLECTION);
		wizard.editbox(1).setText(namePrefix + ITEM_SUFFIX);
		SummaryPage item = wizard.save().publish();

		item.editCataloguesPage().addToWhiteList(CATALOGUE);

		ItemPricingTiersPage tp = item.editPricingTiersPage();
		SubscriptionTierRow subscriptionTier = tp.getSubscriptionTier(SUBSCRIPTION_TIER);
		weekPrice = subscriptionTier.getValue("Week");
		monthPrice = subscriptionTier.getValue("Month");
		yearPrice = subscriptionTier.getValue("Year");
		subscriptionTier.select();
		tp.selectOutrightPricingTier(OUTRIGHT_PRICING_TIER);
		tp.save();
	}

	@Test(dependsOnMethods = {"setupItemTest"})
	public void checkStoreFront()
	{
		PageContext context = newContext("storefront");

		logon(context, "autotest", "automated");
		BrowseCataloguePage browseCataloguePage = new ShopPage(context).load().pickStoreSingleCatalogue(STORE_NAME);
		browseCataloguePage.setQuery(namePrefix + ITEM_SUFFIX);
		CatalogueResourcePage page = browseCataloguePage.search().getResult(1).viewSummary();

		assertTrue(page.isSubandOutright());
		Assert.assertEquals(page.getPerUnitOutrightPrice(),
			OUTRIGHT_PRICING_TIER.substring(OUTRIGHT_PRICING_TIER.length() - 9));
		// 9 is the length of '$X.XX CUR'

		page.setSubscriptionModel();
		// week should be selected as default
		Assert.assertEquals(page.getSelectedDurationPrice(), weekPrice);
		// select month
		page.changeDuration(2);
		Assert.assertEquals(page.getSelectedDurationPrice(), monthPrice);
		// select year
		page.changeDuration(5);
		Assert.assertEquals(page.getSelectedDurationPrice(), yearPrice);
	}

	@Test(dependsOnMethods = {"setupItemTest", "checkStoreFront"})
	public void freeOverrideTest()
	{
		logon("autotest", "automated");
		SummaryPage summaryPage = new SearchPage(context).load().search(namePrefix + ITEM_SUFFIX).getResult(1)
			.viewSummary();

		summaryPage.editPricingTiersPage().setFree(true).save();

		logout();

		PageContext context = newContext("storefront");
		logon(context, "autotest", "automated");

		BrowseCataloguePage browseCataloguePage = new ShopPage(context).load().pickStoreSingleCatalogue(STORE_NAME)
			.get();
		browseCataloguePage.setQuery(namePrefix + ITEM_SUFFIX);
		CatalogueResourcePage page = browseCataloguePage.search().getResult(1).viewSummary();

		Assert.assertFalse(page.isSubandOutright());
		Assert.assertTrue(page.isFree());
	}

}
