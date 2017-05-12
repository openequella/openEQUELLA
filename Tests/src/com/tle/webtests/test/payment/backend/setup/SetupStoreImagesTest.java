package com.tle.webtests.test.payment.backend.setup;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.net.URL;

import org.testng.annotations.Test;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.ReceiptPage;
import com.tle.webtests.pageobject.SettingsPage;
import com.tle.webtests.pageobject.payment.backend.StoreSetupPage;
import com.tle.webtests.pageobject.payment.storefront.BrowseCataloguePage;
import com.tle.webtests.pageobject.payment.storefront.ShopPage;
import com.tle.webtests.test.AbstractCleanupTest;
import com.tle.webtests.test.files.Attachments;
import com.tle.webtests.test.payment.global.RegisterStoreAndStoreFront;

/**
 * @see DTEC #017964
 * @author Seb
 */

@TestInstitution("storebackendssl")
public class SetupStoreImagesTest extends AbstractCleanupTest
{

	private static String REST_SOURCE = "api/store/icon/large.jpg";

	@Test
	public void setupImages()
	{
		logon("autotest", "automated");

		StoreSetupPage page = new SettingsPage(context).load().storeSetupPage();

		URL icon = Attachments.get("shopicon.jpeg");
		URL image = Attachments.get("shopimage.jpeg");

		page.uploadIcon(icon);
		assertTrue(page.hasIcon());
		page.uploadImage(image);
		assertTrue(page.hasImage());

		page.save();
		ReceiptPage.waiter("Settings saved successfully", page).get();

		logout();

	}

	// FIXME: Synchronisation
	@Test(dependsOnMethods = {"setupImages"})
	public void checkStoreFront()
	{
		final String STORE_NAME = RegisterStoreAndStoreFront.STORE_NAME;

		PageContext context = newContext("storefront");
		logon(context, "autotest", "automated");
		ShopPage shopPage = new ShopPage(context).load();
		String imageSrc = shopPage.getStoreImageSrc(STORE_NAME);
		assertTrue(imageSrc.contains(REST_SOURCE), "Store image '" + imageSrc + "' should contain " + REST_SOURCE);
		BrowseCataloguePage browseCatPage = shopPage.pickStoreSingleCatalogue(STORE_NAME);
		assertTrue(browseCatPage.checkStoreIcon("storebackendssl"));

	}

	@Test(dependsOnMethods = {"setupImages", "checkStoreFront"})
	public void deleteImages()
	{
		logon("autotest", "automated");
		StoreSetupPage page = new SettingsPage(context).load().storeSetupPage();
		page.deleteIcon();
		page.deleteImage();

		page.save();

		logout();
	}

	@Test(dependsOnMethods = {"deleteImages"})
	public void checkImagesDeleted()
	{
		final String STORE_NAME = RegisterStoreAndStoreFront.STORE_NAME;

		PageContext context = newContext("storefront");
		logon(context, "autotest", "automated");
		ShopPage shopPage = new ShopPage(context).load();
		String imageSrc = shopPage.getStoreImageSrc(STORE_NAME);
		assertFalse(imageSrc.contains(REST_SOURCE), "Store image '" + imageSrc + "' should not contain " + REST_SOURCE);
		BrowseCataloguePage browseCatPage = shopPage.pickStoreSingleCatalogue(STORE_NAME);
		assertFalse(browseCatPage.checkStoreIcon("storebackendssl"));

	}
}
