package com.tle.webtests.test.payment.backend;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.SettingsPage;
import com.tle.webtests.pageobject.payment.backend.EditTierPage;
import com.tle.webtests.pageobject.payment.backend.ShowTiersPage;
import com.tle.webtests.test.AbstractCleanupTest;

/**
 * @author Aaron
 */
@TestInstitution("ecommerce")
public class PricingTierTest extends AbstractCleanupTest
{
	private static final String PURCHASE_TIER_1 = "Purchase tier 1";

	private static final String SUB_TIER_1 = "Subscription tier 1";
	private static final String SUB_TIER_2 = "Subscription tier ╩ôFÌ 2";

	private static final String CLONE_TIER_NAME = "Dolly";
	private static final String CLONE_TIER_DESC = "Baaaa";
	private static final String CLONED_TIER = "Copy of " + CLONE_TIER_NAME;

	/**
	 * DTEC 17886
	 */
	@Test
	public void testTierList()
	{
		logon("AutoTest", "automated");

		ShowTiersPage page = new SettingsPage(context).load().pricingTierSettings();

		// page.setCurrency("AUD");
		page.enablePurchase(true);

		EditTierPage editPage = page.createTier(false);
		editPage.setName(PURCHASE_TIER_1);
		editPage.setDescription("A description");
		editPage.setPrice(0, "Five bucks");
		editPage = editPage.saveInvalidFields();
		Assert.assertTrue(editPage.isPeriodInvalid(0), "Invalid price message expekted");
		editPage.setPrice(0, "0");
		editPage = editPage.saveInvalidFields();
		Assert.assertTrue(editPage.isPeriodInvalid(0), "Invalid price message expekted");
		editPage.setPrice(0, "-1");
		editPage = editPage.saveInvalidFields();
		Assert.assertTrue(editPage.isPeriodInvalid(0), "Invalid price message expekted");
		editPage.setPrice(0, "1000000001");
		editPage = editPage.saveInvalidFields();
		Assert.assertTrue(editPage.isPeriodInvalid(0), "Invalid price message expekted");
		editPage.setPrice(0, "0.34");
		page = editPage.save();

		// assert tiers showing

		page.enableSubscription(true);

		editPage = page.createTier(true);
		editPage.setName(SUB_TIER_1);
		editPage.setDescription("A description");
		editPage.enablePeriod(0, true);
		editPage.setPrice(0, "1.33");
		editPage.enablePeriod(4, true);
		editPage.setPrice(4, "4.33");

		page = editPage.save();

		editPage = page.createTier(true);
		editPage.setName(SUB_TIER_2);
		editPage.enablePeriod(1, true);
		editPage.setPrice(1, "1");
		editPage.enablePeriod(2, true);
		editPage.setPrice(2, "2");
		// bodgy price
		editPage.enablePeriod(3, true);
		editPage.setPrice(3, "TRY PROGRAMMING YOUR OWN SOUNDS");

		editPage = editPage.saveInvalidFields();

		// assert invalid message
		Assert.assertTrue(editPage.isPeriodInvalid(3), "Invalid price message expexted");

		editPage.setPrice(3, "-1");

		editPage = editPage.saveInvalidFields();

		// assert invalid message
		Assert.assertTrue(editPage.isPeriodInvalid(3), "Invalid price message expexted");

		// fix the problem
		editPage.setPrice(3, "3");
		// turn on a period, enter no price
		editPage.enablePeriod(4, true);
		// save again
		editPage.saveInvalidFields();
		Assert.assertTrue(editPage.isPeriodInvalid(4), "Invalid price message expexted");
		Assert.assertFalse(editPage.isPeriodInvalid(3), "Invalid price message not expexted");
		// de-select all fields
		editPage.disableAllPeriods();
		// save again
		editPage.saveInvalidFields();
		// check error message
		Assert.assertTrue(editPage.isNoSubTierErrorPresent());
		// enter lengthy price price
		editPage.enablePeriod(0, true);
		editPage.setPrice(0, "12.3456");
		editPage.saveInvalidFields();
		Assert.assertTrue(editPage.isPeriodInvalid(0));
		editPage.setPrice(0, "12.50");
		// delete title
		editPage.setName("");
		editPage.saveInvalidFields();
		Assert.assertTrue(editPage.isNameInvalid());
		// save finally
		editPage.setName(SUB_TIER_2);
		page = editPage.save();

		// assert tiers showing
		Assert.assertTrue(page.tierExists(PURCHASE_TIER_1, false), "Pricing tier not found");
		Assert.assertTrue(page.tierExists(SUB_TIER_1, true), "Pricing tier not found");
		Assert.assertTrue(page.tierExists(SUB_TIER_2, true), "Pricing tier not found");

		page.deleteTier(PURCHASE_TIER_1, false, false);
		page.deleteTier(SUB_TIER_1, true, false);
		page.deleteTier(SUB_TIER_2, true, false);
	}

	@Test
	public void cloneTest()
	{
		logon("Autotest", "automated");
		ShowTiersPage page = new ShowTiersPage(context).load();
		EditTierPage editPage = page.createTier(true);
		editPage.setName(CLONE_TIER_NAME);
		editPage.setDescription(CLONE_TIER_DESC);
		editPage.enablePeriod(2, true);
		editPage.enablePeriod(4, true);
		editPage.setPrice(2, "20");
		editPage.setPrice(4, "44.44");
		page = editPage.save();

		editPage = page.cloneTier(CLONE_TIER_NAME, true);
		Assert.assertEquals(CLONED_TIER, editPage.getName());
		Assert.assertEquals(CLONE_TIER_DESC, editPage.getDescription());
		Assert.assertEquals("20.00", editPage.getPrice(2));
		Assert.assertEquals("44.44", editPage.getPrice(4));
		page = editPage.save();

		Assert.assertTrue(page.tierExists(CLONE_TIER_NAME, true));
		Assert.assertTrue(page.tierExists(CLONED_TIER, true));
		page.deleteTier(CLONE_TIER_NAME, true, false);
		page.deleteTier(CLONED_TIER, true, false);
	}

	@Override
	protected void cleanupAfterClass() throws Exception
	{
		logon("AutoTest", "automated");
		ShowTiersPage page = new SettingsPage(context).load().pricingTierSettings();

		page.enablePurchase(true);
		page.enableSubscription(true);

		if( page.tierExists(PURCHASE_TIER_1, false) )
		{
			page.deleteTier(PURCHASE_TIER_1, false, false);
		}
		if( page.tierExists(SUB_TIER_1, true) )
		{
			page.deleteTier(SUB_TIER_1, true, false);
		}
		if( page.tierExists(CLONE_TIER_NAME, true) )
		{
			page.deleteTier(CLONE_TIER_NAME, true, false);
		}
		if( page.tierExists(CLONED_TIER, true) )
		{
			page.deleteTier(CLONED_TIER, true, false);
		}

		// page.setCurrency("USD");
	}
}
