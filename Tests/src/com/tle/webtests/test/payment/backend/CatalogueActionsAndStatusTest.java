package com.tle.webtests.test.payment.backend;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.tle.webtests.framework.Name;
import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.PrefixedName;
import com.tle.webtests.pageobject.SettingsPage;
import com.tle.webtests.pageobject.generic.component.ShuffleBox;
import com.tle.webtests.pageobject.payment.backend.CataloguesBulkDialog;
import com.tle.webtests.pageobject.payment.backend.EditCataloguePage;
import com.tle.webtests.pageobject.payment.backend.ItemCataloguesPage;
import com.tle.webtests.pageobject.payment.backend.ShowCataloguesPage;
import com.tle.webtests.pageobject.searching.ItemAdminPage;
import com.tle.webtests.pageobject.searching.ItemListPage;
import com.tle.webtests.pageobject.searching.SearchPage;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.test.AbstractCleanupTest;

/**
 * @author Dinuk Catalogue - Actions / Status - TestID #17912
 */

@TestInstitution("ecommerce")
public class CatalogueActionsAndStatusTest extends AbstractCleanupTest
{
	@Name(value = "ItemDynamic", group = "items")
	private static PrefixedName DYNAMIC_ITEM_NAME;
	@Name("ItemDynamic Desc")
	private static PrefixedName DYNAMIC_ITEM_DESC;
	@Name("ItemDynamic Content")
	private static PrefixedName DYNAMIC_ITEM_CONTENT;
	@Name(value = "Dynamic Test Catalogue 1", group = "cats")
	private static PrefixedName DYNAMIC_CATALOGUE_NAME;
	@Name("Dynamic Test Catalogue 1 Desc")
	private static PrefixedName DYNAMIC_CATALOGUE_DESC;
	@Name(value = "Static Test Catalogue 1", group = "cats")
	private static PrefixedName STATIC_CATALOGUE_NAME;
	@Name("Static Test Catalogue 1 Desc")
	private static PrefixedName STATIC_CATALOGUE_DESC;

	private static String DYNAMIC_COLLECTION_TO_CONTRIBUTE = "Dynamic Test Collection";
	private static String DYNAMIC_COLLECTION_NAME = "Test Dyna";

	@Test
	public void setUpDynamicCataloguesAndItems()
	{
		logon("AutoTest", "automated");

		// Test Catalogues Creation

		SettingsPage settingsPage = new SettingsPage(context).load();
		assertTrue(settingsPage.isSettingVisible("Catalogues"));
		ShowCataloguesPage cataloguesPage = settingsPage.showCataloguesPage();

		EditCataloguePage editCat = cataloguesPage.createCatalogue();
		editCat.setName(DYNAMIC_CATALOGUE_NAME);
		editCat.setDescription(DYNAMIC_CATALOGUE_DESC);
		editCat.setEnabled(true);
		editCat.setDynamicCollection(DYNAMIC_COLLECTION_NAME);
		cataloguesPage = editCat.save();
		assertTrue(cataloguesPage.entityExists(DYNAMIC_CATALOGUE_NAME));

		editCat = cataloguesPage.createCatalogue();
		editCat.setName(STATIC_CATALOGUE_NAME);
		editCat.setDescription(STATIC_CATALOGUE_DESC);
		editCat.setEnabled(true);
		cataloguesPage = editCat.save();
		assertTrue(cataloguesPage.entityExists(STATIC_CATALOGUE_NAME));

		// Test Item Creation -- DONE
		ContributePage contributePage = new ContributePage(context).load();
		assertTrue(contributePage.hasCollection(DYNAMIC_COLLECTION_TO_CONTRIBUTE));
		WizardPageTab wizardPageTab = contributePage.openWizard(DYNAMIC_COLLECTION_TO_CONTRIBUTE);
		wizardPageTab.editbox(1, DYNAMIC_ITEM_NAME);
		wizardPageTab.editbox(2, DYNAMIC_ITEM_DESC);
		wizardPageTab.editbox(3, DYNAMIC_ITEM_CONTENT);
		wizardPageTab.save().publish();

		logout();
	}

	@Test(dependsOnMethods = "setUpDynamicCataloguesAndItems")
	public void addResource()
	{
		logon("AutoTest", "automated");
		ItemListPage itemListPage;
		ItemCataloguesPage editCatalogues;

		// Search for the Item in the Dynamic Collection
		SearchPage searchPage = new SearchPage(context).load();
		searchPage.setWithinCollection(DYNAMIC_COLLECTION_NAME);
		searchPage.search();
		itemListPage = searchPage.results();

		// View Edit Catelogue Page of Item via Search in Dynamic Collection

		assertTrue(itemListPage.doesResultExist(DYNAMIC_ITEM_NAME));
		SummaryPage summaryTabPage = itemListPage.viewFromTitle(DYNAMIC_ITEM_NAME.toString());
		editCatalogues = summaryTabPage.editCataloguesPage();

		// Verify Dynamic Categloue & Properties
		assertTrue(editCatalogues.isIncluded(DYNAMIC_CATALOGUE_NAME));
		assertTrue(editCatalogues.catalogueExists(DYNAMIC_CATALOGUE_NAME));
		assertEquals(editCatalogues.catalogueStatus(DYNAMIC_CATALOGUE_NAME), "Auto");
		assertTrue(editCatalogues.canExclude(DYNAMIC_CATALOGUE_NAME));

		// Verify Static Catalogue & Properties
		assertTrue(editCatalogues.isExcluded(STATIC_CATALOGUE_NAME));
		assertTrue(editCatalogues.catalogueExists(STATIC_CATALOGUE_NAME));
		assertEquals(editCatalogues.catalogueStatus(STATIC_CATALOGUE_NAME), "None");
		assertTrue(editCatalogues.canAdd(STATIC_CATALOGUE_NAME));

		// Change Action of Catalogue with Dynamic Collection
		editCatalogues.exclude(DYNAMIC_CATALOGUE_NAME);

		assertTrue(editCatalogues.canUnexclude(DYNAMIC_CATALOGUE_NAME));

		// Access Manage Resources Page
		ItemAdminPage manageResourcesPage = new ItemAdminPage(context).load();

		// Search for Items
		manageResourcesPage.setQuery(DYNAMIC_ITEM_NAME.toString());
		itemListPage = manageResourcesPage.results();

		// Verify Item Detection
		assertTrue(itemListPage.doesResultExist(DYNAMIC_ITEM_NAME));

		// Select Items
		itemListPage.setChecked(DYNAMIC_ITEM_NAME.toString(), true);

		// Access Bulk Action Window
		CataloguesBulkDialog catDialog = manageResourcesPage.bulk().addCatalogues();
		ShuffleBox catBox = catDialog.getCatalogueShuffleBox();
		catBox.moveRightByText(DYNAMIC_CATALOGUE_NAME.toString());

		// Execute Bulk Action
		assertTrue(catDialog.executeBulk().waitAndFinish(manageResourcesPage));

		// Access Item Summary After Bulk Action
		manageResourcesPage.setQuery(DYNAMIC_ITEM_NAME);
		itemListPage = manageResourcesPage.results();

		// Verify Item Detection
		assertTrue(itemListPage.doesResultExist(DYNAMIC_ITEM_NAME));

		summaryTabPage = itemListPage.viewFromTitle(DYNAMIC_ITEM_NAME);
		editCatalogues = summaryTabPage.editCataloguesPage();

		// Verify Catalogue Add Status after Bulk Actions
		assertTrue(editCatalogues.isIncluded(DYNAMIC_CATALOGUE_NAME));
		assertTrue(editCatalogues.catalogueExists(DYNAMIC_CATALOGUE_NAME));
		assertEquals(editCatalogues.catalogueStatus(DYNAMIC_CATALOGUE_NAME), "Manual");
		assertTrue(editCatalogues.canRemove(DYNAMIC_CATALOGUE_NAME));

		// Remove from Manual Additions
		editCatalogues.removeFromWhiteList(DYNAMIC_CATALOGUE_NAME);

		// Check After Remove from Manual Addtions
		assertTrue(editCatalogues.isIncluded(DYNAMIC_CATALOGUE_NAME));
		assertTrue(editCatalogues.catalogueExists(DYNAMIC_CATALOGUE_NAME));
		assertEquals(editCatalogues.catalogueStatus(DYNAMIC_CATALOGUE_NAME), "Auto");
		assertTrue(editCatalogues.canExclude(DYNAMIC_CATALOGUE_NAME));

		// Verify Static Catalogue & Properties
		assertTrue(editCatalogues.isExcluded(STATIC_CATALOGUE_NAME));
		assertTrue(editCatalogues.catalogueExists(STATIC_CATALOGUE_NAME));
		assertTrue(editCatalogues.catalogueStatus(STATIC_CATALOGUE_NAME).equals("None"));
		assertTrue(editCatalogues.canAdd(STATIC_CATALOGUE_NAME));

		// Change Add to Catalogue Action and Verify Alert Message
		editCatalogues.addToWhiteList(STATIC_CATALOGUE_NAME);
		// Is 'Remove from manual additions' link existing
		assertTrue(editCatalogues.canRemove(STATIC_CATALOGUE_NAME));
		// Change Remove from manual additions Action and Verify Alert Message
		editCatalogues.removeFromWhiteList(STATIC_CATALOGUE_NAME);
		// Is 'Add to catalogue' link existing
		assertTrue(editCatalogues.canAdd(STATIC_CATALOGUE_NAME));

		// Bulk Action - Remove from Catalogues

		manageResourcesPage = new ItemAdminPage(context).load();

		// Search for Items
		manageResourcesPage.setQuery(DYNAMIC_ITEM_NAME.toString());
		itemListPage = manageResourcesPage.results();

		// Verify Item Detection
		assertTrue(itemListPage.doesResultExist(DYNAMIC_ITEM_NAME));

		// Select Items
		itemListPage.setChecked(DYNAMIC_ITEM_NAME.toString(), true);

		// Access Bulk Action Window
		catDialog = manageResourcesPage.bulk().removeCatalogues();
		catBox = catDialog.getCatalogueShuffleBox();
		catBox.setSelectionByText(DYNAMIC_CATALOGUE_NAME.toString(), STATIC_CATALOGUE_NAME.toString());

		// Execute Bulk Action
		assertTrue(catDialog.executeBulk().waitAndFinish(manageResourcesPage));

		// Access Item Summary After Bulk Action
		manageResourcesPage.setQuery(DYNAMIC_ITEM_NAME.toString());
		itemListPage = manageResourcesPage.results();

		// Verify Item Detection
		assertTrue(itemListPage.doesResultExist(DYNAMIC_ITEM_NAME));

		summaryTabPage = itemListPage.viewFromTitle(DYNAMIC_ITEM_NAME);
		editCatalogues = summaryTabPage.editCataloguesPage();

		// Verify Catalogue Status For the Item After Bulk Removal
		assertTrue(editCatalogues.catalogueExists(DYNAMIC_CATALOGUE_NAME));
		assertTrue(editCatalogues.catalogueExists(STATIC_CATALOGUE_NAME));
		assertTrue(editCatalogues.canUnexclude(DYNAMIC_CATALOGUE_NAME));
		assertTrue(editCatalogues.canUnexclude(STATIC_CATALOGUE_NAME));

		// Assign Dynamic Collection to a Static Catalogue
		SettingsPage settingsPage = new SettingsPage(context).load();
		assertTrue(settingsPage.isSettingVisible("Catalogues"));
		ShowCataloguesPage cataloguesPage = settingsPage.showCataloguesPage();
		EditCataloguePage editCataloguePage = cataloguesPage.editCatalogue(DYNAMIC_CATALOGUE_NAME);
		editCataloguePage.setDynamicCollection(DYNAMIC_COLLECTION_NAME);
		cataloguesPage = editCataloguePage.save();

		// Verify Catelogue Actions After Editing Static Collection
		manageResourcesPage = new ItemAdminPage(context).load();
		manageResourcesPage.setQuery(DYNAMIC_ITEM_NAME);
		itemListPage = manageResourcesPage.results();

		// Verify Item Detection
		assertTrue(itemListPage.doesResultExist(DYNAMIC_ITEM_NAME));

		summaryTabPage = itemListPage.viewFromTitle(DYNAMIC_ITEM_NAME.toString());
		editCatalogues = summaryTabPage.editCataloguesPage();

		// Verify Catalogue Status For the Item After Bulk Removal
		assertTrue(editCatalogues.catalogueExists(DYNAMIC_CATALOGUE_NAME));
		assertTrue(editCatalogues.catalogueExists(STATIC_CATALOGUE_NAME));
		assertTrue(editCatalogues.canUnexclude(DYNAMIC_CATALOGUE_NAME));
		assertTrue(editCatalogues.canUnexclude(STATIC_CATALOGUE_NAME));

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
