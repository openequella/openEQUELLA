package com.tle.webtests.test.payment.backend;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.payment.backend.ShowTiersPage;
import com.tle.webtests.test.AbstractSessionTest;


/**
 * @author Dinuk
 * eCommerce - Cross-institution tier pollution  - TestID #17916 [Support Test]
 */

@TestInstitution("ecommerce2")
public class CrossInstitutionTierPollutionSupportTest extends AbstractSessionTest{

	private final String purchasetiername = "E-CommerceII Purchase Price Tier 1";
	private final String purchasetiercostwithCurrency = "$10.00 USD";
	private final String subscriptiontiername = "E-CommerceII Subscription Price Tier 1";
	private final String subscriptiontiercostwithCurrency = "$10.00 USD";
	
	@Test
	public void verifyPricingTiers()
	{
		logon("AutoTest", "automated");
		
		//Test Price Tier Creation - Access Pricing Tiers Page
		ShowTiersPage pricingTiersPage = new ShowTiersPage(context).load();
				
		//Verify Settings
		Assert.assertFalse(pricingTiersPage.isFree());
		Assert.assertTrue(pricingTiersPage.isPurchase());
		Assert.assertTrue(pricingTiersPage.isSubscribe());
						
		//Verify the purchase tier added
		Assert.assertTrue(pricingTiersPage.tierExists(purchasetiername, false));
		Assert.assertTrue(pricingTiersPage.getPriceForPurchaseTier(purchasetiername)
			.equals(purchasetiercostwithCurrency));
		
		//Verify Subscription Tier Added 
		Assert.assertTrue(pricingTiersPage.tierExists(subscriptiontiername, true));
		
		//Verify value added for Subscription Tier
		Assert.assertTrue(pricingTiersPage.getPriceForSubscriptionTier(subscriptiontiername, "Year").equals(
			subscriptiontiercostwithCurrency));
		
		logout();
	}
}
