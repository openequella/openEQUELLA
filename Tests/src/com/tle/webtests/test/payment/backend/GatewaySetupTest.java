package com.tle.webtests.test.payment.backend;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.tle.webtests.framework.Name;
import com.tle.webtests.framework.PageContext;
import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.PrefixedName;
import com.tle.webtests.pageobject.SettingsPage;
import com.tle.webtests.pageobject.payment.backend.EditGatewayPage;
import com.tle.webtests.pageobject.payment.backend.FakeGatewayEditor;
import com.tle.webtests.pageobject.payment.backend.PaypalGatewayEditor;
import com.tle.webtests.pageobject.payment.backend.ShowGatewaysPage;
import com.tle.webtests.pageobject.payment.storefront.BrowseCataloguePage;
import com.tle.webtests.pageobject.payment.storefront.CartViewPage;
import com.tle.webtests.pageobject.payment.storefront.CatalogueResourcePage;
import com.tle.webtests.pageobject.payment.storefront.ShopPage;
import com.tle.webtests.test.AbstractCleanupTest;
import com.tle.webtests.test.payment.global.RegisterStoreAndStoreFront;

/**
 * @see DTEC: #017997, #017956 Redmine: #7009
 * @author Dustin
 */
@TestInstitution("storebackendssl")
public class GatewaySetupTest extends AbstractCleanupTest
{
	@Name("paypal")
	private static PrefixedName PP_NAME;
	@Name("paypal not yet")
	private static PrefixedName PP_NAME_NOT_YET;
	private static final String PP_DESC = "Payment gateway made by an auto-test, if you can see it that's bad (it should have been deleted)";
	private static final String PP_UN = "dustin_1343192618_biz@equella.com";

	@Name("fake")
	private static PrefixedName F_NAME;
	@Name("fake not yet")
	private static PrefixedName F_NAME_NOT_YET;
	private static final String F_DESC = "Payment gateway made by an auto-test, if you can see it that's bad (it should have been deleted)";

	// Use these for other tests if you want, if you want to change settings:
	// Username: equellasella@gmail.com
	// Password: equellasell

	private static final String STORE_NAME = RegisterStoreAndStoreFront.STORE_NAME;
	private static final String OLD_ITEM = "Resource from the past";

	@Test
	public void setupPaypalTest()
	{
		logon("autotest", "automated");

		SettingsPage settingsPage = new SettingsPage(context).load();
		ShowGatewaysPage gatewaysPage = settingsPage.gatewaysPage();
		EditGatewayPage editPage = gatewaysPage.createNewGateway();

		PaypalGatewayEditor pge = editPage.setTypetoPaypal();
		pge.setName(PP_NAME);
		pge.setDescription(PP_DESC);
		pge.saveWithErrors();

		pge.setName(null);
		pge.setUsername(PP_UN);
		pge.saveWithErrors();

		pge.setName(PP_NAME_NOT_YET);
		pge.setEnabled(true);
		// #7009
		gatewaysPage = pge.save();
		pge = gatewaysPage.editPaypalGateway(PP_NAME_NOT_YET);
		pge.setName(PP_NAME);
		pge.save();
	}

	@Test(dependsOnMethods = "setupPaypalTest")
	public void verifyPaypalGatewayStore()
	{
		// Test from the store
		logon("autotest", "automated");

		SettingsPage settingsPage = new SettingsPage(context).load();
		ShowGatewaysPage gatewaysPage = settingsPage.gatewaysPage();
		PaypalGatewayEditor pge = gatewaysPage.editPaypalGateway(PP_NAME);

		assertEquals(pge.getName(), PP_NAME.toString());
		assertEquals(pge.getDescription(), PP_DESC);
		assertEquals(pge.getUsername(), PP_UN);
		assertTrue(pge.isSandbox());
		pge.cancel();
	}

	@Test
	public void setupFakeTest()
	{
		logon("autotest", "automated");

		SettingsPage settingsPage = new SettingsPage(context).load();
		ShowGatewaysPage gatewaysPage = settingsPage.gatewaysPage();
		EditGatewayPage editPage = gatewaysPage.createNewGateway();

		FakeGatewayEditor fge = editPage.setTypetoFake();
		fge.setDescription(F_DESC);
		fge.setName(null);
		fge.saveWithErrors();

		fge.setName(F_NAME_NOT_YET);

		fge.setEnabled(false);
		fge.setNoDelay(true);
		fge.save();
		// #7009
		fge = gatewaysPage.editFakeGateway(F_NAME_NOT_YET);
		fge.setName(F_NAME);
		fge.save();
	}

	@Test(dependsOnMethods = "setupFakeTest")
	public void verifyFakeGatewayStore()
	{
		// Test from the store
		logon("autotest", "automated");

		SettingsPage settingsPage = new SettingsPage(context).load();
		ShowGatewaysPage gatewaysPage = settingsPage.gatewaysPage();

		FakeGatewayEditor fge = gatewaysPage.editFakeGateway(F_NAME);
		assertEquals(fge.getName(), F_NAME.toString());
		assertEquals(fge.getDescription(), F_DESC);
		assertTrue(fge.isNoDelay());
		fge.cancel();
	}

	@Test(dependsOnMethods = "verifyPaypalGatewayStore")
	public void verifyGatewaysStorefront()
	{
		PageContext context2 = newContext("storefront");
		logon("autotest", "automated");

		SettingsPage settingsPage = new SettingsPage(context).load();
		ShowGatewaysPage gatewaysPage = settingsPage.gatewaysPage();
		PaypalGatewayEditor pge = gatewaysPage.editPaypalGateway(PP_NAME);
		pge.setEnabled(false);
		pge.save();

		logout();

		logon(context2, "gatewayTestUser", "equella");

		ShopPage shopPage = new ShopPage(context2).load();
		BrowseCataloguePage browseCatPage = shopPage.pickStoreSingleCatalogue(STORE_NAME);
		CatalogueResourcePage resourcePage = browseCatPage.search(OLD_ITEM).getResult(1).viewSummary();
		resourcePage.addToCart();

		CartViewPage cartView = resourcePage.viewCart();
		assertFalse(cartView.getStoreSection(STORE_NAME).isPaypalButtonPresent());

		logout(context2);

		logon("autotest", "automated");

		settingsPage = new SettingsPage(context).load();
		gatewaysPage = settingsPage.gatewaysPage();
		pge = gatewaysPage.editPaypalGateway(PP_NAME);
		pge.setEnabled(true);
		gatewaysPage = pge.save();

		logout();

		logon(context2, "gatewayTestUser", "equella");

		shopPage = new ShopPage(context2).load();
		browseCatPage = shopPage.pickStoreSingleCatalogue(STORE_NAME);
		resourcePage = browseCatPage.search(OLD_ITEM).getResult(1).viewSummary();

		cartView = resourcePage.viewCart();
		assertTrue(cartView.getStoreSection(STORE_NAME).isPaypalButtonPresent());

		shopPage = new ShopPage(context2).load();
		browseCatPage = shopPage.pickStoreSingleCatalogue(STORE_NAME);
		resourcePage = browseCatPage.search(OLD_ITEM).getResult(1).viewSummary();
		resourcePage.removeFromCart();

		logout(context2);
	}

	@Override
	protected void cleanupAfterClass() throws Exception
	{
		logon("autotest", "automated");
		SettingsPage settingsPage = new SettingsPage(context).load();

		ShowGatewaysPage gatewaysPage = settingsPage.gatewaysPage();
		gatewaysPage.deleteAllNamed(getNames());

		super.cleanupAfterClass();
	}
}
