package com.tle.webtests.test.payment.global;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.SettingsPage;
import com.tle.webtests.pageobject.oauth.OAuthLogonPage;
import com.tle.webtests.pageobject.payment.backend.EditStoreFrontRegistrationPage;
import com.tle.webtests.pageobject.payment.backend.ShowStoreFrontRegistrationsPage;
import com.tle.webtests.pageobject.payment.storefront.StoreRegistrationEditPage;
import com.tle.webtests.pageobject.payment.storefront.StoreRegistrationPage;
import com.tle.webtests.test.AbstractCleanupTest;

/**
 * @see DTEC: #017939, #017940, #017943, #017944
 * @author Seb
 */
@TestInstitution("storefront")
public class RegisterStoreAndStoreFront extends AbstractCleanupTest
{
	private static final String APPLICATION = "EQUELLA";
	private static final String VERSION = "6.0";
	private static final String COUNTRY_CODE = "AU";
	private static final String REDIRECT_URL = "access/registerstore.do";
	private static final String STORE_FRONT_USER = "AutoTest";

	public static final String STOREFRONT_NAME = "eCommerce Autotest Store Front";
	public static final String STOREFRONT_NAME_TAXED = "eCommerce Autotest Taxed Store Front";

	public static final String STORE_NAME = "eCommerce Autotest Store Backend SSL";
	public static final String STORE_NAME2 = "eCommerce Autotest Store Backend 2 SSL";
	public static final String STORE_BREAKABLE_NAME = "eCommerce Autotest Store Breakable SSL";

	public static final String CLIENT_ID_STOREFRONT = "st";
	public static final String CLIENT_ID_STOREFRONT_TAXED = "st_taxed";

	public static final String INSTITUTION_STOREFRONT = "storefront";
	public static final String INSTITUTION_STOREFRONT_TAXED = "taxedstorefront";

	public static final String INSTITUTION_STORE = "storebackendssl";
	public static final String INSTITUTION_STORE2 = "storebackend2ssl";
	public static final String INSTITUTION_STORE_BREAKABLE = "storebreakablessl";

	/**
	 * 1st: storefront institution to configure within<br>
	 * 2nd: store institution<br>
	 * 3rd: the store name
	 */
	@DataProvider(name = "storeUrls", parallel = false)
	public Object[][] storeUrls()
	{
		return new Object[][]{{INSTITUTION_STOREFRONT, INSTITUTION_STORE, STORE_NAME, CLIENT_ID_STOREFRONT},
				{INSTITUTION_STOREFRONT, INSTITUTION_STORE2, STORE_NAME2, CLIENT_ID_STOREFRONT},
				{INSTITUTION_STOREFRONT, INSTITUTION_STORE_BREAKABLE, STORE_BREAKABLE_NAME, CLIENT_ID_STOREFRONT},
				{INSTITUTION_STOREFRONT_TAXED, INSTITUTION_STORE, STORE_NAME, CLIENT_ID_STOREFRONT_TAXED}};
	}

	/**
	 * 1st: store institution backend to configure within<br>
	 * 2nd: storefront institution
	 */
	@DataProvider(name = "storeFrontUrls", parallel = false)
	public Object[][] storeFrontUrls()
	{
		return new Object[][]{{INSTITUTION_STORE, INSTITUTION_STOREFRONT, STOREFRONT_NAME, CLIENT_ID_STOREFRONT},
				{INSTITUTION_STORE2, INSTITUTION_STOREFRONT, STOREFRONT_NAME, CLIENT_ID_STOREFRONT},
				{INSTITUTION_STORE_BREAKABLE, INSTITUTION_STOREFRONT, STOREFRONT_NAME, CLIENT_ID_STOREFRONT},
				{INSTITUTION_STORE, INSTITUTION_STOREFRONT_TAXED, STOREFRONT_NAME_TAXED, CLIENT_ID_STOREFRONT_TAXED}};
	}

	@Test(dataProvider = "storeFrontUrls")
	public void testStoreFrontRegistration(String storeUrl, String storeFrontInstitution, String storeFrontName,
		String storeFrontClientId)
	{
		PageContext context = newContext(storeUrl);
		logon(context, "AutoTest", "automated");

		ShowStoreFrontRegistrationsPage regoPage = new SettingsPage(context).load().storeFrontRegisterPage();
		EditStoreFrontRegistrationPage page;

		if( regoPage.regoExists(storeFrontName) )
		{
			page = regoPage.editRegistration(storeFrontName);
			page.setClientId(storeFrontClientId);
			page.setRedirectUrl(context.getTestConfig().getInstitutionUrl(storeFrontInstitution) + REDIRECT_URL);
			page.save();
		}
		else
		{
			page = regoPage.createRegistration();
			page.setName(storeFrontName);
			page.setPricingModels(true, true, true);
			page.setApplication(APPLICATION);
			page.setVersion(VERSION);
			page.setCountry(COUNTRY_CODE);
			page.setClientId(storeFrontClientId);
			page.setRedirectUrl(context.getTestConfig().getInstitutionUrl(storeFrontInstitution) + REDIRECT_URL);
			page.setStoreFrontUser(STORE_FRONT_USER);
			assertEquals(page.getUserValidationMessage(), null, "User field was not valid");
			page.setEnabled(true);
			regoPage = page.save();
		}
		assertTrue(regoPage.regoExists(storeFrontName));
		logout(context);
	}

	@Test(dataProvider = "storeUrls", dependsOnMethods = {"testStoreFrontRegistration"})
	public void testStoreRegistration(String storeFrontUrl, String backendUrl, String storeName, String clientId)
	{
		PageContext context = newContext(storeFrontUrl);
		logon(context, "Autotest", "automated");

		StoreRegistrationPage regoPage = new SettingsPage(context).load().storeRegistrationsPage();
		StoreRegistrationEditPage page;

		if( regoPage.checkEntryExists(storeName) )
		{
			page = regoPage.editStoreRego(storeName);
			page.setClientId(clientId);
			page.setStoreUrl(context.getTestConfig().getInstitutionUrl(backendUrl));

			OAuthLogonPage connectPage = page.connectToStore();
			page = connectPage.logon("automated", new StoreRegistrationEditPage(context, false));

			regoPage = page.saveStoreRegistration();
		}
		else
		{
			page = regoPage.createRegistration();

			page.setClientId(clientId);
			page.setStoreUrl(context.getTestConfig().getInstitutionUrl(backendUrl));

			OAuthLogonPage connectPage = page.connectToStore();
			// Title changes (probably shouldn't)
			page = connectPage.logon("automated", new StoreRegistrationEditPage(context, false));

			page.waitForCheckbox();
			assertFalse(page.isSaveButtonHidden());
			page.enableStore();

			regoPage = page.saveStoreRegistration();
		}
		assertTrue(regoPage.checkEntryExists(storeName));

		logout();
	}

}
