package com.tle.webtests.test.payment.storefront;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.SettingsPage;
import com.tle.webtests.pageobject.payment.storefront.StoreRegistrationEditPage;
import com.tle.webtests.pageobject.payment.storefront.StoreRegistrationPage;
import com.tle.webtests.test.AbstractCleanupAutoTest;
import com.tle.webtests.test.payment.global.RegisterStoreAndStoreFront;

/**
 * @author Seb
 * @see Split from RegisterStoreAndStoreFrontTest
 */

@TestInstitution("storefront")
public class StoreRegistrationValidationTest extends AbstractCleanupAutoTest
{
	private static final String URL_INVALID = "You must enter a valid Store URL";
	private static final String URL_NO_STORE = "This EQUELLA instance has not been enabled as a store";
	private static final String URL_BAD_RESPONSE = "You must enter a valid Store URL (Unexpected response from server)";
	private static final String URL_ALREADY_REGISTERED = "The URL has already been registered";
	private static final String INSTITUTION_STORE = RegisterStoreAndStoreFront.INSTITUTION_STORE;
	
	@Test
	public void testStoreRegoValidation()
	{
		logon("autotest", "automated");

		StoreRegistrationPage regoPage = new SettingsPage(context).load().storeRegistrationsPage();
		StoreRegistrationEditPage page = regoPage.createRegistration();
		page.setStoreUrl("http://whatever.com");
		page.setClientId("0111087886");
		assertTrue(page.isSaveButtonHidden());
		regoPage = page.cancelStoreRegistration();

		page = regoPage.createRegistration();
		page.setClientId("");
		page.setStoreUrl("");
		page.connectWithErrors();
		assertTrue(page.isClientIdInvalid());
		assertTrue(page.isURLInvalid());
		page.setClientId("st");
		page.setStoreUrl("wwwww");
		page.connectWithErrors();
		assertTrue(page.isURLInvalid());
		assertEquals(page.getInvalidURLMessage(), URL_INVALID);
		page.setStoreUrl(context.getTestConfig().getServerUrl(false) + "ecommerce");
		page.connectWithErrors();
		assertTrue(page.isURLInvalid());
		assertEquals(page.getInvalidURLMessage(), URL_BAD_RESPONSE);
		page.setStoreUrl(context.getTestConfig().getServerUrl() + INSTITUTION_STORE + "/notreal");
		page.connectWithErrors();
		assertTrue(page.isURLInvalid());
		assertEquals(page.getInvalidURLMessage(), URL_BAD_RESPONSE);
		page.setStoreUrl(context.getTestConfig().getServerUrl(true) + INSTITUTION_STORE);
		page.connectWithErrors();
		assertTrue(page.isURLInvalid());
		assertEquals(page.getInvalidURLMessage(), URL_ALREADY_REGISTERED);
		
		//TODO: valid url with invalid token
		page.cancelStoreRegistration();

		logout();
		
	}
}
