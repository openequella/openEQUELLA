package com.tle.webtests.test.payment.storefront;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.SettingsPage;
import com.tle.webtests.pageobject.payment.backend.EditStoreFrontRegistrationPage;
import com.tle.webtests.pageobject.payment.backend.StoreSetupPage;
import com.tle.webtests.pageobject.payment.storefront.StoreRegistrationDetailsPage;
import com.tle.webtests.test.AbstractCleanupTest;
import com.tle.webtests.test.payment.global.RegisterStoreAndStoreFront;

/**
 * @see DTEC: 017940
 * @author Seb
 */

@TestInstitution("storefront")
public class StoreDetailsTest extends AbstractCleanupTest
{
	private static final String STORE_NAME = RegisterStoreAndStoreFront.STORE_NAME;
	private static final String STOREFRONT_NAME = RegisterStoreAndStoreFront.STOREFRONT_NAME;
	private static final String CLIENT_ID = RegisterStoreAndStoreFront.CLIENT_ID_STOREFRONT;
	private static final String STORE_URL = RegisterStoreAndStoreFront.INSTITUTION_STORE;
	private String contactName;
	private String contactNumber;
	private String contactEmail;
	private boolean freeAllowed;
	private boolean outrightAllowed;
	private boolean subAllowed;

	@Test
	public void testStoreDetails()
	{
		logon("autotest", "automated");

		StoreRegistrationDetailsPage page = new SettingsPage(context).load().storeRegistrationsPage()
			.viewStoreDetail(STORE_NAME);

		Assert.assertEquals(page.getStoreName(), STORE_NAME);
		Assert.assertEquals(page.getURL(), context.getTestConfig().getInstitutionUrl(STORE_URL));
		Assert.assertEquals(page.getClientId(), CLIENT_ID);
		Assert.assertTrue(page.isEnabled());

		contactName = page.getContactName();
		contactNumber = page.getContactNumber();
		contactEmail = page.getContactEmail();

		freeAllowed = page.isFreeAllowed();
		outrightAllowed = page.isOutrightAllowed();
		subAllowed = page.isSubAllowed();

		logout();
	}

	@Test(dependsOnMethods = {"testStoreDetails"})
	public void testContactDetails()
	{
		PageContext context = newContext(STORE_URL);
		logon(context, "autotest", "automated");

		StoreSetupPage page = new SettingsPage(context).load().storeSetupPage();

		Assert.assertEquals(page.getContactName(), contactName);
		Assert.assertEquals(page.getContactNumber(), contactNumber);
		Assert.assertEquals(page.getContactEmail(), contactEmail);

	}

	@Test(dependsOnMethods = {"testStoreDetails"})
	public void testTransactionsAllowed()
	{
		PageContext context = newContext(STORE_URL);
		logon(context, "autotest", "automated");

		EditStoreFrontRegistrationPage page = new SettingsPage(context).load().storeFrontRegisterPage()
			.editRegistration(STOREFRONT_NAME);

		Assert.assertEquals(page.isFreeChecked(), freeAllowed);
		Assert.assertEquals(page.isSubscriptionChecked(), subAllowed);
		Assert.assertEquals(page.isOutrightChecked(), outrightAllowed);
	}
}
