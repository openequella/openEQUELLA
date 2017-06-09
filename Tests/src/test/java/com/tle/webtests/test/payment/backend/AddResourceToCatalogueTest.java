package com.tle.webtests.test.payment.backend;

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
 * @author Dinuk Check Add a Resource to Catalogue - TestID #17884
 */

@TestInstitution("ecommerce")
public class AddResourceToCatalogueTest extends AbstractCleanupTest
{
	@Name("StoreUserCatelogue1")
	private static PrefixedName CAT1_NAME;
	@Name("StoreUserCatelogue2")
	private static PrefixedName CAT2_NAME;

	@Test
	public void setUpCataloguesAndItems()
	{
		logon("AutoTest", "automated");

		// Test Catalogues Creation

		SettingsPage settingsPage = new SettingsPage(context).load();
		ShowCataloguesPage cataloguesPage = settingsPage.showCataloguesPage();

		EditCataloguePage editCat = cataloguesPage.createCatalogue();
		editCat.setName(CAT1_NAME);
		editCat.setDescription("StoreUserCatelogue1 Desc");
		editCat.setEnabled(true);
		cataloguesPage = editCat.save();
		assertTrue(cataloguesPage.entityExists(CAT1_NAME));

		editCat = cataloguesPage.createCatalogue();
		editCat.setName(CAT2_NAME);
		editCat.setDescription("StoreUserCatelogue2 Desc");
		editCat.setEnabled(true);
		cataloguesPage = editCat.save();
		assertTrue(cataloguesPage.entityExists(CAT2_NAME));

		logon("storeuser", "equella");

		// Test Item Creation....
		for( int count = 1; count <= 3; count++ )
		{
			ContributePage contributePage = new ContributePage(context).load();
			assertTrue(contributePage.hasCollection("Test Collection"));
			WizardPageTab wizardPageTab = contributePage.openWizard("Test Collection");
			wizardPageTab.editbox(1, "AddResourceToCatalogueTest" + count);
			wizardPageTab.editbox(2, "AddResourceToCatalogueTest Desc" + count);
			wizardPageTab.editbox(3, "AddResourceToCatalogueTest Content" + count);
			wizardPageTab.save().publish();
		}

	}

	@Test(dependsOnMethods = "setUpCataloguesAndItems")
	public void addResource()
	{
		logon("storeuser", "equella");

		// Access Search Page and Locate Item
		SearchPage searchPage = new SearchPage(context).load();
		searchPage.setQuery("AddResourceToCatalogueTest1");
		searchPage.search();
		ItemListPage itemListPage = searchPage.results();

		// Verify Item Detection
		assertTrue(itemListPage.doesResultExist("AddResourceToCatalogueTest1"));

		// Access Item Summary Page of an Item
		SummaryPage summaryTabPage = itemListPage.viewFromTitle("AddResourceToCatalogueTest1");

		// View Edit Catelogue Page
		ItemCataloguesPage itemCataloguesPage = summaryTabPage.editCataloguesPage();

		// Change Add to Catalogue Action and Verify Alert Message
		itemCataloguesPage.addToWhiteList(CAT1_NAME);

		// Change Remove from manual additions Action and Verify Alert Message
		itemCataloguesPage.removeFromWhiteList(CAT1_NAME);

		// Is 'Add to catalogue' link existing
		assertTrue(itemCataloguesPage.canAdd(CAT1_NAME));

		// Access Manage Resources Page
		ItemAdminPage manageResourcesPage = new ItemAdminPage(context).load();

		// Search for Items
		manageResourcesPage.setQuery("AddResourceToCatalogueTest*");
		itemListPage = manageResourcesPage.results();

		// Select Items
		itemListPage.setChecked("AddResourceToCatalogueTest2", true);
		itemListPage.setChecked("AddResourceToCatalogueTest3", true);

		// Access Bulk Action Window
		CataloguesBulkDialog catDialog = manageResourcesPage.bulk().addCatalogues();
		ShuffleBox catBox = catDialog.getCatalogueShuffleBox();

		// Verify and Move Catalgoue 1 & 2
		catBox.setSelectionByText(CAT1_NAME.toString(), CAT2_NAME.toString());

		// Execute Bulk Action
		assertTrue(catDialog.executeBulk().waitAndFinish(manageResourcesPage));

		// Verify whether items are successully moved into catalogues - Item 1
		itemListPage = manageResourcesPage.results();
		summaryTabPage = itemListPage.viewFromTitle("AddResourceToCatalogueTest2");
		itemCataloguesPage = summaryTabPage.editCataloguesPage();
		assertTrue(itemCataloguesPage.canRemove(CAT1_NAME));
		assertTrue(itemCataloguesPage.canRemove(CAT2_NAME));

		// Removing the item from Catalogues
		itemCataloguesPage.removeFromWhiteList(CAT1_NAME);
		assertTrue(itemCataloguesPage.canAdd(CAT1_NAME));

		itemCataloguesPage.removeFromWhiteList(CAT2_NAME);
		assertTrue(itemCataloguesPage.canAdd(CAT2_NAME));

		// Verify whether items are successully moved into catalogues - Item 2
		manageResourcesPage = new ItemAdminPage(context).load();
		manageResourcesPage.setQuery("AddResourceToCatalogueTest*");
		itemListPage = manageResourcesPage.results();
		summaryTabPage = itemListPage.viewFromTitle("AddResourceToCatalogueTest3");
		itemCataloguesPage = summaryTabPage.editCataloguesPage();
		assertTrue(itemCataloguesPage.canRemove(CAT1_NAME));
		assertTrue(itemCataloguesPage.canRemove(CAT2_NAME));

		// Removing the item from Catalogues
		itemCataloguesPage.removeFromWhiteList(CAT1_NAME);
		assertTrue(itemCataloguesPage.canAdd(CAT1_NAME));

		itemCataloguesPage.removeFromWhiteList(CAT2_NAME);
		assertTrue(itemCataloguesPage.canAdd(CAT2_NAME));
	}

	@Override
	protected void cleanupAfterClass() throws Exception
	{
		logon("AutoTest", "automated");
		ShowCataloguesPage cataloguesPage = new SettingsPage(context).load().showCataloguesPage();
		// Delete Catalogue 1
		cataloguesPage.deleteAllNamed(getNames());
		super.cleanupAfterClass();
	}
}
