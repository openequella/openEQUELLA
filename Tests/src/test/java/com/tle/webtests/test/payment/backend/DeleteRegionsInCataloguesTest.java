package com.tle.webtests.test.payment.backend;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.tle.webtests.framework.Name;
import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.PrefixedName;
import com.tle.webtests.pageobject.SettingsPage;
import com.tle.webtests.pageobject.payment.backend.EditCataloguePage;
import com.tle.webtests.pageobject.payment.backend.EditRegionPage;
import com.tle.webtests.pageobject.payment.backend.ShowCataloguesPage;
import com.tle.webtests.pageobject.payment.backend.ShowRegionsPage;
import com.tle.webtests.test.AbstractSessionTest;

/**
 * @author Dinuk Delete regions which are in catalogues - TestID #17913
 */

@TestInstitution("ecommerce")
public class DeleteRegionsInCataloguesTest extends AbstractSessionTest
{
	@Name(value = "TEST REGION 1", group = "regions")
	private static PrefixedName REGION_TITLE;
	public static String REGION_DESC = "TEST REGION 1 DESC";
	public static String PREF_REGION = "Oceania";

	@Name(value = "Test Catalogue 1", group = "cats")
	private static PrefixedName CATALOGUE_NAME;
	public static String CATALOGUE_DESC = "Test Catalogue 1 Desc";

	@Override
	protected void prepareBrowserSession()
	{
		logon("AutoTest", "automated");
	}

	@Test
	public void setUpRegionsAndCatalogues()
	{
		// Test Region Creation
		SettingsPage settingsPage = new SettingsPage(context).load();
		ShowRegionsPage showRegionsPage = settingsPage.showRegionPage();
		EditRegionPage createRegionPage = showRegionsPage.createRegion();
		createRegionPage.setName(REGION_TITLE);
		createRegionPage.setDescription(REGION_DESC);
		createRegionPage.selectPredefinedRegion(PREF_REGION);
		createRegionPage.save();
		showRegionsPage = new ShowRegionsPage(context).load();
		Assert.assertTrue(showRegionsPage.entityExists(REGION_TITLE));

		// Test Catalogue Creation with Region
		settingsPage = new SettingsPage(context).load();
		Assert.assertTrue(settingsPage.isSettingVisible("Catalogues"));
		ShowCataloguesPage cataloguesPage = settingsPage.showCataloguesPage();
		EditCataloguePage editCat = cataloguesPage.createCatalogue();
		editCat.setName(CATALOGUE_NAME);
		editCat.setDescription(CATALOGUE_DESC);
		editCat.setEnabled(true);
		editCat.setRestrictToRegions(true);
		editCat.setRegions(true, REGION_TITLE);
		editCat.save();
	}

	@Test(dependsOnMethods = "setUpRegionsAndCatalogues")
	public void validateDeleteRegion()
	{
		SettingsPage settingsPage = new SettingsPage(context).load();
		ShowRegionsPage showRegionsPage = settingsPage.showRegionPage();

		// Verify Region Cannot be Deleted
		showRegionsPage.disableEntity(REGION_TITLE);
		showRegionsPage.deleteRegionFail(REGION_TITLE);
		// grr, need to re-enable so that it shows on catalogue screen
		showRegionsPage.enableEntity(REGION_TITLE);
		Assert.assertTrue(showRegionsPage.entityExists(REGION_TITLE));

		// Remove Region from Catalogue
		settingsPage = new SettingsPage(context).load();
		ShowCataloguesPage cataloguesPage = settingsPage.showCataloguesPage();
		EditCataloguePage editCataloguesPage = cataloguesPage.editCatalogue(CATALOGUE_NAME);
		editCataloguesPage.setRegions(false, REGION_TITLE);
		editCataloguesPage.save();

		// Verify Region Can Be Deleted
		settingsPage = new SettingsPage(context).load();
		showRegionsPage = settingsPage.showRegionPage();
		showRegionsPage.disableEntity(REGION_TITLE);
		showRegionsPage.deleteEntity(REGION_TITLE);
		Assert.assertFalse(showRegionsPage.entityExists(REGION_TITLE));
	}

	@Override
	protected void cleanupAfterClass() throws Exception
	{
		logon("AutoTest", "automated");

		SettingsPage settingsPage = new SettingsPage(context).load();

		settingsPage = new SettingsPage(context).load();
		ShowCataloguesPage cataloguesPage = settingsPage.showCataloguesPage();
		cataloguesPage.deleteAllNamed(getNames("cats"));

		ShowRegionsPage showRegionsPage = settingsPage.showRegionPage();
		showRegionsPage.deleteAllNamed(getNames("regions"));
	}
}
