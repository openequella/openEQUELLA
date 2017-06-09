package com.tle.webtests.test.payment.backend;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.payment.backend.ItemPricingTiersPage;
import com.tle.webtests.pageobject.payment.backend.ItemPricingTiersPage.SubscriptionTierRow;
import com.tle.webtests.pageobject.payment.backend.ShowTiersPage;
import com.tle.webtests.pageobject.searching.ItemListPage;
import com.tle.webtests.pageobject.searching.SearchPage;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.test.AbstractCleanupTest;

/**
 * @author Dinuk Check Add a Resource to Catalogue - TestID #17914
 */

@TestInstitution("ecommerce")
public class DisplayPricingInResourceTest extends AbstractCleanupTest
{

	private final String purchasetiername = "Purchase Price Tier 1";
	private final String purchasetierdesc = "Purchase Price Tier 1 Desc";
	private final String purchasetiercost = "10.00";
	private final String purchasetiercostwithCurrency = "$10.00 USD";
	private final String subscriptiontiername = "Subscription Price Tier 1";
	private final String subscriptiontierdesc = "Subscription Price Tier 1 Desc";
	private final String subscriptiontiercost = "10.00";
	private final String subscriptiontiercostwithCurrency = "$10.00 USD";

	@Override
	protected void prepareBrowserSession()
	{
		logon("priceuser", "equella");
	}

	// Login and Setup Test Data
	@Test
	public void setUpTestTiersAndItems()
	{
		// --Test Data Creation

		// Test Price Tier Creation - Access Pricing Tiers Page
		ShowTiersPage pricingTiersPage = new ShowTiersPage(context).load();

		pricingTiersPage.enablePurchase(true);
		pricingTiersPage.enableSubscription(true);

		// Test Price Tier Creation - Purchase Price Tier Creation
		pricingTiersPage = pricingTiersPage.createTier(false).setName(purchasetiername)
			.setDescription(purchasetierdesc).setPrice(0, purchasetiercost).save();

		// Verify the purchase tier added
		Assert.assertTrue(pricingTiersPage.tierExists(purchasetiername, false));
		Assert.assertTrue(pricingTiersPage.getPriceForPurchaseTier(purchasetiername).equals(
			purchasetiercostwithCurrency));

		// Create Subscription Tier
		pricingTiersPage = pricingTiersPage.createTier(true).setName(subscriptiontiername)
			.setDescription(subscriptiontierdesc).enablePeriod(4, true).setPrice(4, subscriptiontiercost).save();

		// Verify Subscription Tier Added
		Assert.assertTrue(pricingTiersPage.tierExists(subscriptiontiername, true));

		// Verify value added for Subscription Tier
		Assert.assertTrue(pricingTiersPage.getPriceForSubscriptionTier(subscriptiontiername, "Year").equals(
			subscriptiontiercostwithCurrency));

		// Test Item Creation....
		Integer cnt = new Integer(1);
		ContributePage contributePage = new ContributePage(context).load();
		Assert.assertTrue(contributePage.hasCollection("Price Tier Test Collection"));
		WizardPageTab wizardPageTab = contributePage.openWizard("Price Tier Test Collection");
		wizardPageTab.editbox(1, "DisplayPricingInResourceTest".concat(cnt.toString()));
		wizardPageTab.editbox(2, "DisplayPricingInResourceTest Desc".concat(cnt.toString()));
		wizardPageTab.editbox(3, "DisplayPricingInResourceTest Content".concat(cnt.toString()));
		wizardPageTab.save().publish();
	}

	@Test(dependsOnMethods = "setUpTestTiersAndItems")
	public void displayPricingItemSummaryAndResults()
	{
		// --Search Item

		// Access Search Page and Locate Item
		SearchPage searchPage = new SearchPage(context).load();
		searchPage.setQuery("DisplayPricingInResourceTest1");
		searchPage.search();
		ItemListPage itemListPage = searchPage.results();

		// Verify Item Detection
		Assert.assertTrue(itemListPage.doesResultExist("DisplayPricingInResourceTest1"));

		// Access Item Summary Page of an Item
		SummaryPage summaryTabPage = itemListPage.viewFromTitle("DisplayPricingInResourceTest1");

		// Is Set Pricing Tiers Command Existing
		Assert.assertTrue(summaryTabPage.hasAction("Set pricing tiers"));

		// Access Set Pricing Tiers Page
		ItemPricingTiersPage selectPricingTiersPage = summaryTabPage.editPricingTiersPage();

		// Check Select Pricing Tier UI
		Assert.assertTrue(selectPricingTiersPage.isPurchaseTierListPresent());
		Assert.assertTrue(selectPricingTiersPage.isSubscriptionTierTablePresent());

		// Check Pricing Tier Presence
		Assert.assertTrue(selectPricingTiersPage.isPurchaseTierPresent(purchasetiername + " "
			+ purchasetiercostwithCurrency));

		// Check Created Subscription Tier Presence
		Assert.assertTrue(selectPricingTiersPage.getSubscriptions().contains(subscriptiontiername));

		// Check Subscription value with Time Period
		SubscriptionTierRow subscriptionTier = selectPricingTiersPage.getSubscriptionTier(subscriptiontiername);
		Assert.assertTrue(subscriptionTier.getValue("Year").equals(subscriptiontiercostwithCurrency));

		// Check Subscription Not Selected
		Assert.assertFalse(subscriptionTier.isSelected());

		// Select Purchase Price Tier
		selectPricingTiersPage.selectOutrightPricingTier(purchasetiername + " " + purchasetiercostwithCurrency);

		// Subscription Selection
		subscriptionTier.select();

		// Save Pricing & Subscription Tiers
		selectPricingTiersPage.save();
		Assert.assertTrue(selectPricingTiersPage.confirmSaveMessage().equals(ItemPricingTiersPage.RECEIPT_SAVE));

		// Search and Access Item Summary
		searchPage = new SearchPage(context).load();
		searchPage.setQuery("DisplayPricingInResourceTest1");
		searchPage.search();
		itemListPage = searchPage.results();
		Assert.assertTrue(itemListPage.doesResultExist("DisplayPricingInResourceTest1"));
		summaryTabPage = itemListPage.viewFromTitle("DisplayPricingInResourceTest1");

		// Check Pricing Tier Presence
		Assert.assertTrue(summaryTabPage.isPricingTierTitlePresent("Pricing tiers"));
		Assert.assertTrue(summaryTabPage.isPricingTierPresent(purchasetiername));

		// Check Subscription Tier Presence
		Assert.assertTrue(summaryTabPage.verifyPricingTierAddedAtIndex(1).equals(subscriptiontiername));

		// Check Subscription value with Time Period
		Assert.assertTrue(summaryTabPage.verifyPricingTierValueAddedAtIndex(1, 6).equals(
			subscriptiontiercostwithCurrency));

	}

	// Cleanup Data
	@Override
	protected void cleanupAfterClass() throws Exception
	{
		logon("AutoTest", "automated");

		// Unassign Tiers from Item
		SearchPage searchPage = new SearchPage(context).load();
		searchPage.setQuery("DisplayPricingInResourceTest1");
		searchPage.search();
		ItemListPage itemListPage = searchPage.results();
		if( itemListPage.doesResultExist("DisplayPricingInResourceTest1") )
		{
			SummaryPage summaryTabPage = itemListPage.viewFromTitle("DisplayPricingInResourceTest1");
			ItemPricingTiersPage selectPricingTiersPage = summaryTabPage.editPricingTiersPage();
			Assert.assertTrue(selectPricingTiersPage.isPurchaseTierPresent("Select a pricing tier"));
			selectPricingTiersPage.getSubscriptionTier("No tier").select();
			selectPricingTiersPage.save();
		}

		ShowTiersPage pricingTiersPageDel = new ShowTiersPage(context).load();

		// Delete Purchase Pricing Tiers
		pricingTiersPageDel = pricingTiersPageDel.deleteTier(purchasetiername, false, false);

		// Delete Subscription Pricing Tiers
		pricingTiersPageDel = pricingTiersPageDel.deleteTier(subscriptiontiername, true, false);

		super.cleanupAfterClass();
	}

}
