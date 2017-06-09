package com.tle.webtests.test.payment.backend;


import org.testng.Assert;
import org.testng.annotations.Test;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.payment.backend.ShowTiersPage;
import com.tle.webtests.test.AbstractSessionTest;

/**
 * @author Dinuk
 * eCommerce - Cross-institution tier pollution  - TestID #17916
 */

@TestInstitution("ecommerce")
public class CrossInstitutionTierPollutionTest extends AbstractSessionTest{
	
	private final String purchasetiername = "E-CommerceII Purchase Price Tier 1";
	private final String subscriptiontiername = "E-CommerceII Subscription Price Tier 1";
	
	@Test
	public void validateTiers()
	{
		logon("AutoTest", "automated");
		
		//Test Price Tier Creation - Access Pricing Tiers Page
		ShowTiersPage pricingTiersPage = new ShowTiersPage(context).load();
					
		//Enable Price Tier
		pricingTiersPage.enablePurchase(true);
		
		//Verify the Purchase tier Not Present
		Assert.assertFalse(pricingTiersPage.tierExists(purchasetiername, false));
		
		//Enable Subscription Tier
		pricingTiersPage.enableSubscription(true);
		
		//Verify Subscription Tier Not Present
		Assert.assertFalse(pricingTiersPage.tierExists(subscriptiontiername, true));
		
		logout();
	}
}
