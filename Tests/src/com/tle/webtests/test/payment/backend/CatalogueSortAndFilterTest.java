package com.tle.webtests.test.payment.backend;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.tle.webtests.framework.Name;
import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.PrefixedName;
import com.tle.webtests.pageobject.SettingsPage;
import com.tle.webtests.pageobject.payment.backend.CataloguesBulkDialog;
import com.tle.webtests.pageobject.payment.backend.EditCataloguePage;
import com.tle.webtests.pageobject.payment.backend.ItemCataloguesPage;
import com.tle.webtests.pageobject.payment.backend.ItemPricingTiersPage;
import com.tle.webtests.pageobject.payment.backend.ShowCataloguesPage;
import com.tle.webtests.pageobject.searching.ItemAdminPage;
import com.tle.webtests.pageobject.searching.ItemListPage;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import com.tle.webtests.pageobject.wizard.ConfirmationDialog;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.test.AbstractCleanupTest;

/**
 * @see DTEC: #017951
 * @author Dustin
 */

@TestInstitution("storebackendssl")
public class CatalogueSortAndFilterTest extends AbstractCleanupTest
{
	@Name(value = "catalogue", group = "cats")
	private static PrefixedName CAT_NAME;
	private static final String DYNACOL_NAME = "Dynamic Test Collection";
	private static final String NON_DYNACOL_NAME = "Backend Collection";

	private static final String PURCHASE_TIER = "Outright Purchase $5.00 AUD";

	@Name("live no tier")
	private static PrefixedName LIVE_NO_TIER;
	@Name("live has tier")
	private static PrefixedName LIVE_HAS_TIER;
	@Name("draft not tier")
	private static PrefixedName DRAFT_NO_TIER;
	@Name("draft has tier")
	private static PrefixedName DRAFT_HAS_TIER;
	@Name("blacklisted")
	private static PrefixedName BLACKLISTED;
	@Name("whitelisted")
	private static PrefixedName WHITELISTED;
	@Name("autoincluded")
	private static PrefixedName AUTOINCLUDED;
	@Name("autoincluded and whitelisted")
	private static PrefixedName AUTOINCLUDED_AND_WHITELISTED;

	@Test
	public void catalogueSetup()
	{
		logon("autotest", "automated");

		ShowCataloguesPage catList = new SettingsPage(context).load().showCataloguesPage();
		EditCataloguePage cat = catList.createCatalogue();
		cat.setName(CAT_NAME);
		cat.setEnabled(true);
		cat.setDynamicCollection("Test Dyna");
		cat.save();

		addItem(LIVE_NO_TIER, DYNACOL_NAME, true, null, null, false, false, false);
		addItem(LIVE_HAS_TIER, DYNACOL_NAME, true, PURCHASE_TIER, null, false, false, false);
		addItem(DRAFT_NO_TIER, DYNACOL_NAME, false, null, null, false, false, false);
		addItem(DRAFT_HAS_TIER, DYNACOL_NAME, false, PURCHASE_TIER, null, false, false, false);
		addItem(BLACKLISTED, DYNACOL_NAME, true, PURCHASE_TIER, null, false, false, true);
		addItem(WHITELISTED, NON_DYNACOL_NAME, true, PURCHASE_TIER, null, false, true, false);
		addItem(AUTOINCLUDED, DYNACOL_NAME, true, PURCHASE_TIER, null, false, false, false);
		addItem(AUTOINCLUDED_AND_WHITELISTED, DYNACOL_NAME, true, PURCHASE_TIER, null, false, true, false);

		logout();
	}

	@Test(dependsOnMethods = {"catalogueSetup"})
	public void filterAndSort()
	{
		logon("autotest", "automated");

		ItemAdminPage manageResources = new ItemAdminPage(context).load();
		manageResources.searchWithinCatalogue(CAT_NAME);
		manageResources.exactQuery(prefix());
		manageResources.setCatalogueWhere("pending");
		ItemListPage results = manageResources.results();

		assertTrue(results.doesResultExist(DRAFT_HAS_TIER));
		assertTrue(results.doesResultExist(DRAFT_NO_TIER));
		assertTrue(results.doesResultExist(LIVE_NO_TIER));
		assertEquals(results.getResults().size(), 3);

		results = manageResources.setCatalogueWhere("excluded");

		assertTrue(results.doesResultExist(BLACKLISTED));
		assertEquals(results.getResults().size(), 1);

		manageResources.setCatalogueWhere("live");
		results = manageResources.setManualInclusions(true);

		assertTrue(results.doesResultExist(WHITELISTED));
		assertTrue(results.doesResultExist(AUTOINCLUDED_AND_WHITELISTED));
		assertEquals(results.getResults().size(), 2);

		results = manageResources.setAutoInclusions(true);

		assertTrue(results.doesResultExist(AUTOINCLUDED_AND_WHITELISTED));
		assertEquals(results.getResults().size(), 1);

		results = manageResources.setCatalogueWhere("pending");
		assertEquals(results.getResults().size(), 0);
	}

	// The test case calls for a fair few items, let's make it easier on
	// ourselves
	private void addItem(PrefixedName name, String collection, boolean live, String purchaseTier,
		String subscriptionTier, boolean isFree, boolean whitelist, boolean blacklist)
	{
		WizardPageTab wizard = new ContributePage(context).load().openWizard(collection);
		wizard.editbox(1, name.toString());
		ConfirmationDialog cd = wizard.save();
		SummaryPage stp = null;
		if( !live )
		{
			stp = cd.draft();
		}
		else
		{
			stp = cd.publish();
		}
		ItemPricingTiersPage sptp = stp.editPricingTiersPage();
		if( purchaseTier != null )
		{
			sptp.selectOutrightPricingTier(purchaseTier);
		}
		if( subscriptionTier != null )
		{
			sptp.getSubscriptionTier(subscriptionTier).select();
		}
		sptp.setFree(isFree);
		sptp.save();

		ItemCataloguesPage catPage = stp.editCataloguesPage();
		if( whitelist )
		{
			if( collection.equals(DYNACOL_NAME) )
			{
				ItemAdminPage manageResources = new ItemAdminPage(context).load();
				manageResources.exactQuery(name.toString());
				manageResources.bulk().selectAll();
				CataloguesBulkDialog catDialog = manageResources.bulk().addCatalogues();

				catDialog.getCatalogueShuffleBox().moveRightByText(CAT_NAME.toString());
				assertTrue(catDialog.executeBulk().waitAndFinish(manageResources));
			}
			else
			{
				catPage.addToWhiteList(CAT_NAME);
			}
		}
		else if( blacklist )
		{
			catPage.exclude(CAT_NAME);
		}
	}

	@Override
	protected void cleanupAfterClass() throws Exception
	{
		logon("autotest", "automated");

		ShowCataloguesPage catPage = new SettingsPage(context).load().showCataloguesPage();
		catPage.deleteAllNamed(getNames("cats"));

		super.cleanupAfterClass();
	}
}
