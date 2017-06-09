package com.tle.webtests.test.payment.backend;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.tle.webtests.framework.Name;
import com.tle.webtests.framework.PageContext;
import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.PrefixedName;
import com.tle.webtests.pageobject.SettingsPage;
import com.tle.webtests.pageobject.payment.backend.CataloguesBulkDialog;
import com.tle.webtests.pageobject.payment.backend.EditCataloguePage;
import com.tle.webtests.pageobject.payment.backend.ShowCataloguesPage;
import com.tle.webtests.pageobject.payment.storefront.ShopCataloguesPage;
import com.tle.webtests.pageobject.payment.storefront.ShopPage;
import com.tle.webtests.pageobject.searching.ItemAdminPage;
import com.tle.webtests.test.AbstractCleanupTest;
import com.tle.webtests.test.payment.global.RegisterStoreAndStoreFront;

/**
 * @see DTEC: #017950
 * @author Seb
 */

@TestInstitution("storebackendssl")
public class MultipleCataloguesTest extends AbstractCleanupTest
{
	@Name("cat a")
	private static PrefixedName CAT_A;
	@Name("cat b")
	private static PrefixedName CAT_B;
	@Name("cat c")
	private static PrefixedName CAT_C;
	private int catSize;
	private final static String NO_TIER = "No tier";

	@Test
	public void catalogueSetup() throws Exception
	{
		logon("autotest", "automated");

		// 3 catalouges - 1 with items, 1 empty, 1 disabled
		ShowCataloguesPage catList = new SettingsPage(context).load().showCataloguesPage();

		EditCataloguePage cat = catList.createCatalogue();
		cat.setName(CAT_A);
		cat.setDescription(CAT_A + " desc");
		cat.setEnabled(true);
		catList = cat.save();

		cat = catList.createCatalogue();
		cat.setName(CAT_B);
		cat.setDescription(CAT_B + " desc");
		cat.setEnabled(true);
		catList = cat.save();

		cat = catList.createCatalogue();
		cat.setName(CAT_C);
		cat.setDescription(CAT_C + " desc");
		cat.setEnabled(false);
		catList = cat.save();

		ItemAdminPage manageResources = new ItemAdminPage(context).load();
		manageResources.filterByStatus("live");
		manageResources.setDateFilterBefore("2012-09-12");
		catSize = manageResources.results().getTotalAvailable();
		manageResources.bulk().selectAll();
		CataloguesBulkDialog catDialog = manageResources.bulk().addCatalogues();

		catDialog.getCatalogueShuffleBox().moveRightByText(CAT_A.toString(), CAT_C.toString());
		catDialog.executeBulk().waitAndFinish(manageResources);

		manageResources.filterBySubscriptionTier(NO_TIER);
		manageResources.filterByPurchaseTier(NO_TIER);
		catSize -= manageResources.results().getTotalAvailable();
		manageResources.filterByFree();
		catSize += manageResources.results().getTotalAvailable();

		logout();
	}

	@Test(dependsOnMethods = {"catalogueSetup"})
	public void checkCatalogues()
	{
		final String STORE_NAME = RegisterStoreAndStoreFront.STORE_NAME;
		PageContext context = newContext("storefront");
		logon(context, "autotest", "automated");
		ShopCataloguesPage page = new ShopPage(context).load().pickStore(STORE_NAME);

		Assert.assertEquals(page.getCountForCatalogue(CAT_A), catSize);
		Assert.assertEquals(page.getCountForCatalogue(CAT_B), 0);
		Assert.assertFalse(page.catalogueExists(CAT_C));
	}

	@Override
	protected void cleanupAfterClass() throws Exception
	{
		logon("autotest", "automated");
		ShowCataloguesPage catPage = new SettingsPage(context).load().showCataloguesPage();
		catPage.deleteAllNamed(getNames());
		super.cleanupAfterClass();
	}
}
