package com.tle.webtests.test.payment.backend;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.tle.webtests.framework.Name;
import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.PrefixedName;
import com.tle.webtests.pageobject.SettingsPage;
import com.tle.webtests.pageobject.payment.backend.EditCataloguePage;
import com.tle.webtests.pageobject.payment.backend.ShowCataloguesPage;
import com.tle.webtests.test.AbstractSessionTest;

/**
 * @author Dinuk Deleting Active Catalogues - TestID #17917
 */
@TestInstitution("ecommerce")
public class DeleteActiveCataloguesTest extends AbstractSessionTest
{
	@Name("DeleteActiveCatelogue1")
	private static PrefixedName CATALOGUE_NAME;
	private static final String CATALOGUE_DESC = "DeleteActiveCatelogue1 Desc";

	@Test
	public void setUpCatalogue()
	{
		logon("AutoTest", "automated");

		// Test Catalogues Creation

		SettingsPage settingsPage = new SettingsPage(context).load();
		Assert.assertTrue(settingsPage.isSettingVisible("Catalogues"));
		ShowCataloguesPage cataloguesPage = settingsPage.showCataloguesPage();
		EditCataloguePage editCat = cataloguesPage.createCatalogue();
		editCat.setName(CATALOGUE_NAME);
		editCat.setDescription(CATALOGUE_DESC);
		editCat.setEnabled(true);
		cataloguesPage = editCat.save();
		Assert.assertTrue(cataloguesPage.entityExists(CATALOGUE_NAME));
		logout();
	}

	@Test(dependsOnMethods = "setUpCatalogue")
	public void validateDeleteCatalogue()
	{
		logon("AutoTest", "automated");
		SettingsPage settingsPage = new SettingsPage(context).load();
		Assert.assertTrue(settingsPage.isSettingVisible("Catalogues"));
		ShowCataloguesPage cataloguesPage = settingsPage.showCataloguesPage();

		// Verify Delete Operation
		Assert.assertTrue(cataloguesPage.entityExists(CATALOGUE_NAME));
		Assert.assertFalse(cataloguesPage.isEntityDisabled(CATALOGUE_NAME));
		Assert.assertFalse(cataloguesPage.isDeletable(CATALOGUE_NAME));

		// Disbale Catalogue
		cataloguesPage.disableEntity(CATALOGUE_NAME);
		Assert.assertTrue(cataloguesPage.isEntityDisabled(CATALOGUE_NAME));
		Assert.assertTrue(cataloguesPage.isDeletable(CATALOGUE_NAME));

		// Delete Catalogue
		cataloguesPage.deleteCatalogue(CATALOGUE_NAME);
		logout();
	}

	@Override
	protected void cleanupAfterClass() throws Exception
	{
		logon("AutoTest", "automated");

		SettingsPage settingsPage = new SettingsPage(context).load();
		ShowCataloguesPage cataloguesPage = settingsPage.showCataloguesPage();
		cataloguesPage.deleteAllNamed(getNames());

		super.cleanupAfterClass();
	}
}
