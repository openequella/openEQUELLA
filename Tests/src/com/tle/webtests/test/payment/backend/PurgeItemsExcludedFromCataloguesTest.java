package com.tle.webtests.test.payment.backend;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.tle.webtests.framework.Name;
import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.PrefixedName;
import com.tle.webtests.pageobject.SettingsPage;
import com.tle.webtests.pageobject.payment.backend.EditCataloguePage;
import com.tle.webtests.pageobject.payment.backend.ItemCataloguesPage;
import com.tle.webtests.pageobject.payment.backend.ShowCataloguesPage;
import com.tle.webtests.pageobject.searching.ItemListPage;
import com.tle.webtests.pageobject.searching.SearchPage;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.test.AbstractCleanupTest;

/**
 * @author Dinuk eCommerce - purge items excluded from catalogues - TestID
 *         #17915
 */

@TestInstitution("ecommerce")
public class PurgeItemsExcludedFromCataloguesTest extends AbstractCleanupTest
{
	@Name(value = "ItemDynamic", group = "items")
	private static PrefixedName DYNAMIC_ITEM_NAME;
	@Name("ItemDynamic Desc")
	private static PrefixedName DYNAMIC_ITEM_DESC;
	@Name("ItemDynamic Content")
	private static PrefixedName DYNAMIC_ITEM_CONTENT;

	private static String DYNAMIC_COLLECTION_TO_CONTRIBUTE = "Dynamic Test Collection";
	private static String DYNAMIC_COLLECTION_NAME = "Test Dyna";
	@Name(value = "Dynamic Catalogue 1", group = "cats")
	public static PrefixedName DYNAMIC_CATLALOGUE_NAME;
	public static String DYNAMIC_CATLALOGUE_DESC = "Dynamic Catalogue 1 Desc";

	@Test
	public void setUpCatalogueAndPurgeItem()
	{

		logon("AutoTest", "automated");

		// Test Catalogues Creation

		SettingsPage settingsPage = new SettingsPage(context).load();
		ShowCataloguesPage cataloguesPage = settingsPage.showCataloguesPage();
		EditCataloguePage editCat = cataloguesPage.createCatalogue();
		editCat.setName(DYNAMIC_CATLALOGUE_NAME);
		editCat.setDescription(DYNAMIC_CATLALOGUE_DESC);
		editCat.setDynamicCollection(DYNAMIC_COLLECTION_NAME);
		editCat.setEnabled(true);
		cataloguesPage = editCat.save();
		Assert.assertTrue(cataloguesPage.entityExists(DYNAMIC_CATLALOGUE_NAME));

		// Test Item Creation
		ContributePage contributePage = new ContributePage(context).load();
		Assert.assertTrue(contributePage.hasCollection(DYNAMIC_COLLECTION_TO_CONTRIBUTE));
		WizardPageTab wizardPageTab = contributePage.openWizard(DYNAMIC_COLLECTION_TO_CONTRIBUTE);
		wizardPageTab.editbox(1, DYNAMIC_ITEM_NAME);
		wizardPageTab.editbox(2, DYNAMIC_ITEM_DESC);
		wizardPageTab.editbox(3, DYNAMIC_ITEM_CONTENT);
		wizardPageTab.save().publish();

		logout();

	}

	@Test(dependsOnMethods = "setUpCatalogueAndPurgeItem")
	public void addResource()
	{
		logon("AutoTest", "automated");

		// Access Edit Catalogue Page
		ItemListPage itemListPage;
		ItemCataloguesPage editCataloguesPage;

		// Search for the Item in the Dynamic Collection
		SearchPage searchPage = new SearchPage(context).load();
		searchPage.setWithinCollection(DYNAMIC_COLLECTION_NAME);
		searchPage.search();
		itemListPage = searchPage.results();

		// View Edit Catelogue Page of Item via Search in Dynamic Collection
		Assert.assertTrue(itemListPage.doesResultExist(DYNAMIC_ITEM_NAME));
		SummaryPage summaryTabPage = itemListPage.viewFromTitle(DYNAMIC_ITEM_NAME);
		Assert.assertTrue(summaryTabPage.hasAction("Edit catalogues"));
		editCataloguesPage = summaryTabPage.editCataloguesPage();

		// Verify Dynamic Categloue & Properties
		Assert.assertTrue(editCataloguesPage.isIncluded(DYNAMIC_CATLALOGUE_NAME));
		Assert.assertTrue(editCataloguesPage.catalogueExists(DYNAMIC_CATLALOGUE_NAME));
		Assert.assertTrue(editCataloguesPage.catalogueStatus(DYNAMIC_CATLALOGUE_NAME).equals("Auto"));
		Assert.assertTrue(editCataloguesPage.canExclude(DYNAMIC_CATLALOGUE_NAME));

		// Change Action of Catalogue with Dynamic Collection
		editCataloguesPage.exclude(DYNAMIC_CATLALOGUE_NAME);
		Assert.assertTrue(editCataloguesPage.canUnexclude(DYNAMIC_CATLALOGUE_NAME));

		// Delete the Item and Purge
		summaryTabPage.delete().purge();

		// Verify Item Does not Exist After Purge
		searchPage = new SearchPage(context).load();
		searchPage.setWithinAll();
		searchPage.setWithinCollection(DYNAMIC_COLLECTION_NAME);
		searchPage.search();
		itemListPage = searchPage.results();
		Assert.assertFalse(itemListPage.doesResultExist(DYNAMIC_ITEM_NAME));
	}

	@Override
	protected void cleanupAfterClass() throws Exception
	{
		logon("AutoTest", "automated");
		SettingsPage settingsPage = new SettingsPage(context).load();
		ShowCataloguesPage cataloguesPage = settingsPage.showCataloguesPage();
		cataloguesPage.deleteAllNamed(getNames("cats"));

		super.cleanupAfterClass();
	}

}
