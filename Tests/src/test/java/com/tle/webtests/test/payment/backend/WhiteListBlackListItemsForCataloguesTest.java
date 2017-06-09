package com.tle.webtests.test.payment.backend;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.tle.webtests.framework.Name;
import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.NotPrefixedName;
import com.tle.webtests.pageobject.PrefixedName;
import com.tle.webtests.pageobject.payment.backend.ItemCataloguesPage;
import com.tle.webtests.pageobject.searching.ItemListPage;
import com.tle.webtests.pageobject.searching.SearchPage;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.test.AbstractCleanupTest;

/**
 * @author Dinuk eCommerce - whitelist / blacklist items for catalogues - TestID
 *         #17911
 */

@TestInstitution("ecommerce2")
public class WhiteListBlackListItemsForCataloguesTest extends AbstractCleanupTest
{
	@Name(value = "Item", group = "items")
	private static PrefixedName CAT_ITEM_NAME;
	private static String CAT_ITEM_DESC = "WhiteListBlackListItemsForCataloguesTest Desc";
	private static String CAT_ITEM_CONTENT = "WhiteListBlackListItemsForCataloguesTest Content";

	private static String CAT_COLLECTION_TO_CONTRIBUTE = "Test Collection";

	private static final PrefixedName CATALOGUE_NAME_1 = new NotPrefixedName("Catalogue User 1");
	private static final PrefixedName CATALOGUE_NAME_2 = new NotPrefixedName("Catalogue User 2");

	@Test
	public void setUpItem()
	{
		logon("AutoTest", "automated");

		// Test Item Creation
		ContributePage contributePage = new ContributePage(context).load();
		WizardPageTab wizardPageTab = contributePage.openWizard(CAT_COLLECTION_TO_CONTRIBUTE);
		wizardPageTab.editbox(1, CAT_ITEM_NAME);
		wizardPageTab.editbox(2, CAT_ITEM_DESC);
		wizardPageTab.editbox(3, CAT_ITEM_CONTENT);
		wizardPageTab.save().publish();

		logout();
	}

	@Test(dependsOnMethods = "setUpItem")
	public void addResource()
	{
		logon("catuser2", "equella");

		ItemListPage itemListPage;
		ItemCataloguesPage editCataloguesPage;

		// Access Edit Catalogue Page From Item Summary
		SearchPage searchPage = new SearchPage(context).load();
		searchPage.setWithinCollection(CAT_COLLECTION_TO_CONTRIBUTE);
		itemListPage = searchPage.search();

		// View Edit Catelogue Page of Item via Search in Dynamic Collection
		SummaryPage summaryTabPage = itemListPage.viewFromTitle(CAT_ITEM_NAME);
		editCataloguesPage = summaryTabPage.editCataloguesPage();

		// Verify Catalogue Presence
		Assert.assertTrue(editCataloguesPage.catalogueExists(CATALOGUE_NAME_1));
		Assert.assertTrue(editCataloguesPage.catalogueExists(CATALOGUE_NAME_2));

		Assert.assertTrue(!editCataloguesPage.canAdd(CATALOGUE_NAME_1));
		Assert.assertTrue(editCataloguesPage.canAdd(CATALOGUE_NAME_2));
	}
}
