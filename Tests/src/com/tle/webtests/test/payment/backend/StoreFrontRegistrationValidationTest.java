package com.tle.webtests.test.payment.backend;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.tle.webtests.framework.Name;
import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.PrefixedName;
import com.tle.webtests.pageobject.SettingsPage;
import com.tle.webtests.pageobject.payment.backend.EditStoreFrontRegistrationPage;
import com.tle.webtests.pageobject.payment.backend.ShowStoreFrontRegistrationsPage;
import com.tle.webtests.test.AbstractCleanupTest;

/**
 * @author Seb
 * @see Split from RegisterStoreAndStoreFrontTest
 */
@TestInstitution("storebackendssl")
public class StoreFrontRegistrationValidationTest extends AbstractCleanupTest
{
	@Name("Storefront '''!@╚{§")
	private static PrefixedName STORE_NAME;
	private static final String APPLICATION = "EQUELLA";
	private static final String VERSION = "6.0";
	private static final String COUNTRY_CODE = "AU";
	private static final String CLIENT_ID = "val_st█ÄA";
	private static final String REDIRECT_URL = "storefront/access/registerstore.do";
	private static final String STORE_FRONT_USER = "AutoTest";

	@Test
	public void testValidation()
	{
		logon("AutoTest", "automated");

		ShowStoreFrontRegistrationsPage regoPage = new SettingsPage(context).load().storeFrontRegisterPage();
		EditStoreFrontRegistrationPage page;

		page = regoPage.createRegistration();
		page = page.saveWithErrors();
		assertTrue(page.isNameInvalid());
		page.setName(STORE_NAME);
		page.saveWithErrors();

		assertTrue(page.isTransactionsInvalid());
		page.setPricingModels(true, true, true);
		page = page.saveWithErrors();
		assertFalse(page.isTransactionsInvalid());

		assertEquals(page.getApplicationValidationMessage(),
			EditStoreFrontRegistrationPage.VALIDATION_MISSING_APPLICATION, "Application was not invalid");
		page.setApplication(APPLICATION);
		page = page.saveWithErrors();
		assertEquals(page.getApplicationValidationMessage(), null, "Application was not valid");

		assertEquals(page.getVersionValidationMessage(), EditStoreFrontRegistrationPage.VALIDATION_MISSING_VERSION,
			"Version was not invalid");
		page.setVersion(VERSION);
		page = page.saveWithErrors();
		assertEquals(page.getVersionValidationMessage(), null, "Version was not valid");

		assertEquals(page.getCountryValidationMessage(), EditStoreFrontRegistrationPage.VALIDATION_MISSING_COUNTRY,
			"Country was not invalid");
		page.setCountry(COUNTRY_CODE);
		page = page.saveWithErrors();
		assertEquals(page.getCountryValidationMessage(), null, "Country was not valid");

		assertEquals(page.getClientIdValidationMessage(), EditStoreFrontRegistrationPage.VALIDATION_MISSING_CLIENT_ID,
			"Client ID was not invalid");
		page.setClientId(CLIENT_ID);
		page = page.saveWithErrors();
		assertEquals(page.getClientIdValidationMessage(), null, "Client ID was not valid");

		assertEquals(page.getRedirectUrlValidationMessage(),
			EditStoreFrontRegistrationPage.VALIDATION_MISSING_REDIRECT_URL, "Redirect URL was not invalid");
		page.setRedirectUrl(REDIRECT_URL);
		page = page.saveWithErrors();
		assertEquals(page.getRedirectUrlValidationMessage(), null, "Redirect URL was not valid");

		assertEquals(page.getUserValidationMessage(), EditStoreFrontRegistrationPage.VALIDATION_MISSING_USER,
			"User was not invalid");
		// Make the page still invalid
		page.setClientId("");
		page.setStoreFrontUser(STORE_FRONT_USER);
		page = page.saveWithErrors();
		assertEquals(page.getUserValidationMessage(), null, "User was not valid");

		regoPage = page.cancel();

		assertFalse(regoPage.entityExists(STORE_NAME));

		logout();
	}

	@Override
	protected void cleanupAfterClass() throws Exception
	{
		logon("AutoTest", "automated");

		ShowStoreFrontRegistrationsPage regoPage = new SettingsPage(context).load().storeFrontRegisterPage();
		regoPage.deleteAllNamed(getNames());

		super.cleanupAfterClass();
	}
}
