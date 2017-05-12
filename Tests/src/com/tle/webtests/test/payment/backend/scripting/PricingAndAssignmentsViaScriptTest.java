/**
 * 
 */
package com.tle.webtests.test.payment.backend.scripting;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.List;

import org.testng.annotations.Test;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.NotPrefixedName;
import com.tle.webtests.pageobject.PrefixedName;
import com.tle.webtests.pageobject.SettingsPage;
import com.tle.webtests.pageobject.payment.backend.EditCataloguePage;
import com.tle.webtests.pageobject.payment.backend.EditTierPage;
import com.tle.webtests.pageobject.payment.backend.ItemCataloguesPage;
import com.tle.webtests.pageobject.payment.backend.ScriptedWizardPage;
import com.tle.webtests.pageobject.payment.backend.ShowCataloguesPage;
import com.tle.webtests.pageobject.payment.backend.ShowTiersPage;
import com.tle.webtests.pageobject.searching.ItemListPage;
import com.tle.webtests.pageobject.searching.SearchPage;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import com.tle.webtests.pageobject.wizard.ConfirmationDialog;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.test.AbstractCleanupAutoTest;

/**
 * @author larry
 */
@TestInstitution("ecommerce")
public class PricingAndAssignmentsViaScriptTest extends AbstractCleanupAutoTest
{
	private final static String SCRIPTABLE_COLLECTION_NAME = "Scriptable collection";
	private final static String DEFAULT_CURRENCY = "USD";

	/**
	 * NB: logic of test relies on the Catalogue name not containing whitespace,
	 * because the script will split the entered keywords editbox
	 */
	private final static String CATALOGUE_NAME = "Catalogue";
	private final static String CATALOGUE_DESC = PricingAndAssignmentsViaScriptTest.class.getSimpleName()
		+ " demonstration of catalogue type";

	/**
	 * Having established what the boolean flags on the settings page say about
	 * Free, Purchase and Subscription setting enabled (or not), confirm that
	 * the values extracted via script and displayed as plain text on the
	 * contribution page are in agreement.
	 */
	@Test
	public void performViewGlobalPricingSettings()
	{
		ShowTiersPage pricingTiersPage = new ShowTiersPage(context).load();
		boolean settingsFreeEnabled = pricingTiersPage.isFree();
		boolean settingsPurchaseEnabled = pricingTiersPage.isPurchase();
		boolean settingsSubscriptionEnabled = pricingTiersPage.isSubscribe();
		// Now load up a contribution page and see compare to what the wizard
		// gets via script
		ContributePage contributePage = new ContributePage(context).load();
		WizardPageTab wizard = contributePage.openWizard(SCRIPTABLE_COLLECTION_NAME);
		ScriptedWizardPage scriptedPage = new ScriptedWizardPage(context);
		assertEquals(scriptedPage.isFreeEnabled(), settingsFreeEnabled,
			"Expected Scripted Page to agree with settings ");
		assertEquals(scriptedPage.isPurchaseEnabled(), settingsPurchaseEnabled,
			"Expected Scripted Page to agree with settings ");
		assertEquals(scriptedPage.isSubscriptionEnabled(), settingsSubscriptionEnabled,
			"Expected Scripted Page to agree with settings ");

		wizard.cancel(contributePage);
	}

	@Test(dependsOnMethods = "performViewGlobalPricingSettings")
	public void performManipulateAndRecheckGlobalFreeSetting()
	{
		ShowTiersPage pricingTiersPage = new ShowTiersPage(context).load();
		pricingTiersPage.enableFree(true);
		ContributePage contributePage = new ContributePage(context).load();
		WizardPageTab wizard = contributePage.openWizard(SCRIPTABLE_COLLECTION_NAME);
		ScriptedWizardPage scriptedPage = new ScriptedWizardPage(context);
		boolean freeEnabled = scriptedPage.isFreeEnabled();
		wizard.cancel(contributePage);
		assertTrue(freeEnabled, "Expected Scripted Page to declare Free pricing IS enabled");
	}

	@Test(dependsOnMethods = "performViewGlobalPricingSettings")
	public void performManipulateAndRecheckGlobalPurchaseSetting()
	{
		ShowTiersPage pricingTiersPage = new ShowTiersPage(context).load();
		pricingTiersPage.enablePurchase(true);
		pricingTiersPage.get();
		ContributePage contributePage = new ContributePage(context).load();
		WizardPageTab wizard = contributePage.openWizard(SCRIPTABLE_COLLECTION_NAME);
		ScriptedWizardPage scriptedPage = new ScriptedWizardPage(context);
		boolean purchaseEnabled = scriptedPage.isPurchaseEnabled();
		wizard.cancel(contributePage);
		assertTrue(purchaseEnabled, "Expected Scripted Page to declare Purchase IS enabled");
	}

	@Test(dependsOnMethods = "performViewGlobalPricingSettings")
	public void performManipulateAndRecheckGlobalSubscriptionSetting()
	{
		ShowTiersPage pricingTiersPage = new ShowTiersPage(context).load();
		pricingTiersPage.enableSubscription(true);
		ContributePage contributePage = new ContributePage(context).load();
		WizardPageTab wizard = contributePage.openWizard(SCRIPTABLE_COLLECTION_NAME);
		ScriptedWizardPage scriptedPage = new ScriptedWizardPage(context);
		boolean subscriptionEnabled = scriptedPage.isSubscriptionEnabled();
		wizard.cancel(contributePage);
		assertTrue(subscriptionEnabled, "Expected Scripted Page to declare Subscription IS enabled");
	}

	@Test
	public void contributeItemsAndAssignWhiteOrBlacklist()
	{
		// Create a pair of catalogues, one suffixed with the word 'white', the
		// other with 'black'
		PrefixedName whiteCatalolgueName = createCatalogue("white");
		PrefixedName blackCatalolgueName = createCatalogue("black");

		// Now load up a contribution page and enter among the keywords, an
		// continuous string of the form 'addToWhite' + CATALOGUE_NAME +
		// 'white'. The wizard script is customised to look for a keyword
		// beginning with either 'addToWhite' or 'addToBlack', with the
		// remainder of the word being a catalogue name. On searching through
		// available catalogues, if the script identifies a catalogue with name
		// suffixed in the keyword, it will add it to that catalogues white list
		ContributePage contributePage = new ContributePage(context).load();
		WizardPageTab wizard = contributePage.openWizard(SCRIPTABLE_COLLECTION_NAME);
		String resourceName = this.getClass().getSimpleName() + " resource";
		wizard.editbox(1, resourceName);
		wizard.editbox(2, this.getClass().getSimpleName() + " description");
		ConfirmationDialog confirm = wizard.save();
		confirm.publish();
		// Verify resource added as expected.
		SearchPage searchPage = new SearchPage(context).load();
		searchPage.setQuery(resourceName);
		searchPage.search();
		ItemListPage itemListPage = searchPage.results();

		// Verify Item Detection
		assertTrue(itemListPage.doesResultExist(resourceName));

		// Access Item Summary Page of an Item
		SummaryPage summaryTabPage = itemListPage.viewFromTitle(resourceName);
		// now that the resource has been persisted, we can add it to a
		// catalogue. The script has been customised to do this by virtue of
		// looking for a trigger in the keywords
		summaryTabPage.edit();
		wizard.editbox(3, "addToWhitelist" + whiteCatalolgueName);
		summaryTabPage = wizard.saveNoConfirm();

		// View Edit Catalogue Page
		ItemCataloguesPage editCataloguesPage = summaryTabPage.editCataloguesPage();
		// If the 'remove' link is present, it means the script has successfully
		// added the
		// new resource to the nominated whitelist
		assertTrue(editCataloguesPage.canRemove(whiteCatalolgueName));

		wizard = summaryTabPage.edit();
		wizard.editbox(3, "removeFromWhitelist" + whiteCatalolgueName);
		summaryTabPage = wizard.saveNoConfirm();
		assertTrue(summaryTabPage.editCataloguesPage().canAdd(whiteCatalolgueName));

		wizard = summaryTabPage.edit();
		wizard.editbox(3, "addToBlacklist" + whiteCatalolgueName);
		summaryTabPage = wizard.saveNoConfirm();
		assertTrue(summaryTabPage.editCataloguesPage().canUnexclude(whiteCatalolgueName));

		wizard = summaryTabPage.edit();
		wizard.editbox(3, "removeFromBlacklist" + whiteCatalolgueName);
		summaryTabPage = wizard.saveNoConfirm();
		assertTrue(summaryTabPage.editCataloguesPage().canAdd(whiteCatalolgueName));
	}

	/**
	 * The scriptable collection we have added to the institution is hard-coded
	 * via an Advanced Scripting control, to find and apply a purchase tier
	 * price, and a subscription-tier price bracket. Accordingly once we have
	 * contributed an item, we expect to see proof of the pricing assignment via
	 * that item's summary page (because the scriptable collection also provides
	 * a customised summary script to present that information)
	 */
	@Test(dependsOnMethods = "performManipulateAndRecheckGlobalPurchaseSetting")
	public void createPricingAssignmentOnNewContribution()
	{
		String createdPurchaseTierName = createPurchaseTier();
		ContributePage contributePage = new ContributePage(context).load();
		WizardPageTab wizard = contributePage.openWizard(SCRIPTABLE_COLLECTION_NAME);
		ScriptedWizardPage scriptedPage = new ScriptedWizardPage(context);
		scriptedPage.setInputName(this.getClass().getSimpleName() + ' ' + "createPricingAssignment()");
		scriptedPage.setInputDescription("a description by any other name is still sweet so worldy ahah");
		String purchaseTierUuid = scriptedPage.getPurchaseTierUuidByName(createdPurchaseTierName);
		scriptedPage.setInputPurchaseTierUuid(purchaseTierUuid);
		wizard.save().publish();
		SummaryPage summary = new SummaryPage(context).get();
		List<String> purchaseTierNames = summary.getValuesByCustomClass("purchasetiername");
		assertTrue(purchaseTierNames != null && purchaseTierNames.size() == 1,
			"Expected to find one (only) purchase tier name");
		assertTrue(createdPurchaseTierName.equals(purchaseTierNames.get(0)),
			"Expected to Summary to display purchase tier named " + createdPurchaseTierName);
	}

	@Test(dependsOnMethods = "createPricingAssignmentOnNewContribution")
	public void createPricingTierAssignmentOnExistingContribution()
	{
		String tier = createPurchaseTier(this.getClass().getSimpleName() + " Fee, a frame I haul by shelf");
		String tier2 = createPurchaseTier(this.getClass().getSimpleName() + " bar, a song that's never sung");
		// It's intimidating modifying a Larry test
		ContributePage contributePage = new ContributePage(context).load();
		WizardPageTab wizard = contributePage.openWizard(SCRIPTABLE_COLLECTION_NAME);
		ScriptedWizardPage scriptedPage = new ScriptedWizardPage(context);
		scriptedPage.setInputName(this.getClass().getSimpleName() + ' ' + "createPricingAssignmentExisting()");
		scriptedPage.setInputDescription("\\/\\/ () \\/\\/");
		wizard = wizard.save().publish().edit();
		scriptedPage = new ScriptedWizardPage(context);

		scriptedPage.setInputPurchaseTierUuid(scriptedPage.getPurchaseTierUuidByName(tier));
		SummaryPage previousItem = wizard.saveNoConfirm();
		List<String> purchaseTierNames = previousItem.getValuesByCustomClass("purchasetiername");
		assertEquals(purchaseTierNames.get(0), tier);

		previousItem.editPricingTiersPage().selectOutrightPricingTier("Select a pricing tier").save(); // Clear
		SearchPage searchPage = new SearchPage(context).load();
		previousItem = searchPage.exactQuery(this.getClass().getSimpleName() + ' ' + "createPricingAssignment")
			.getResult(1).viewSummary();

		wizard = previousItem.edit();
		scriptedPage = new ScriptedWizardPage(context);
		scriptedPage.setInputPurchaseTierUuid(scriptedPage.getPurchaseTierUuidByName(tier2));
		previousItem = wizard.saveNoConfirm();
		purchaseTierNames = previousItem.getValuesByCustomClass("purchasetiername");
		assertEquals(purchaseTierNames.get(0), tier2);
	}

	@Test(dependsOnMethods = "createPricingTierAssignmentOnExistingContribution")
	public void setFreeFlagOnNewAndExistingContributions()
	{
		String tier = createSubscriptionTier(this.getClass().getSimpleName() + " Fee, a frame I haul by shelf");

		for( int i = 0; i < 2; i++ )
		{
			// New and existing free only
			WizardPageTab wizard = new ContributePage(context).load().openWizard(SCRIPTABLE_COLLECTION_NAME);
			ScriptedWizardPage scriptedPage = new ScriptedWizardPage(context);
			scriptedPage.setInputName(this.getClass().getSimpleName() + ' '
				+ "setFreeFlagOnNewAndExistingContributions()" + i);
			scriptedPage.setInputDescription("\\/\\/ () \\/\\/");
			wizard.editbox(4, "free");
			if( i == 1 )
			{
				scriptedPage.setInputSubscriptionTierUuid(scriptedPage.getSubscriptionTierUuidByName(tier));
			}
			SummaryPage summary = wizard.save().publish();

			String free = summary.getValuesByCustomClass("freeness").get(0);
			assertTrue(free.equals("Free: true"));

			wizard = summary.edit();
			scriptedPage = new ScriptedWizardPage(context);
			wizard.editbox(4, "notfree");
			summary = wizard.saveNoConfirm();

			free = summary.getValuesByCustomClass("freeness").get(0);
			assertTrue(free.equals("Free: false"));

			wizard = summary.edit();
			scriptedPage = new ScriptedWizardPage(context);
			wizard.editbox(4, "free");
			summary = wizard.saveNoConfirm();

			free = summary.getValuesByCustomClass("freeness").get(0);
			assertTrue(free.equals("Free: true"));
		}
	}

	@Override
	protected void cleanupAfterClass() throws Exception
	{
		// get rid of any items which have a targeted pricing tier assigned to
		// them, before removing the tiers
		super.cleanupAfterClass();
		ShowTiersPage pricingTiersPage = new ShowTiersPage(context).load();
		pricingTiersPage = clearPurchaseTiers(pricingTiersPage);
		clearSubscriptionTiers(pricingTiersPage);
		assertEquals(pricingTiersPage.getCurrency(), DEFAULT_CURRENCY);
		clearCatalogues();
	}

	private ShowTiersPage clearPurchaseTiers(ShowTiersPage pricingTiersPage)
	{
		pricingTiersPage.enablePurchase(true);
		pricingTiersPage = pricingTiersPage.get();

		pricingTiersPage.clearTierWithPrefix(this.getClass().getSimpleName(), false);
		return pricingTiersPage.get();
	}

	private ShowTiersPage clearSubscriptionTiers(ShowTiersPage pricingTiersPage)
	{
		pricingTiersPage.enableSubscription(true);
		pricingTiersPage = pricingTiersPage.get();
		pricingTiersPage.clearTierWithPrefix(this.getClass().getSimpleName(), true);
		return pricingTiersPage.get();
	}

	private void clearCatalogues()
	{
		SettingsPage settingsPage = new SettingsPage(context).load();
		assertTrue(settingsPage.isSettingVisible("Catalogues"));
		ShowCataloguesPage cataloguesPage = settingsPage.showCataloguesPage();
		cataloguesPage.deleteAllNamed(new NotPrefixedName(CATALOGUE_NAME + "black"), new NotPrefixedName(CATALOGUE_NAME
			+ "white"));
	}

	/**
	 * create a disposable purchase tier, its name beginning with this class's
	 * simple name
	 */
	private String createPurchaseTier()
	{
		return createPurchaseTier(this.getClass().getSimpleName() + " toe, a tier");
	}

	private String createPurchaseTier(String name)
	{
		ShowTiersPage pricingTiersPage = new ShowTiersPage(context).load();
		final String tierName = name;
		EditTierPage createPricingTierPage = pricingTiersPage.createTier(false);
		createPricingTierPage.setName(tierName);
		createPricingTierPage.setDescription("arti chokes four for a dollar");
		createPricingTierPage.setPrice(0, "0.25");
		pricingTiersPage = createPricingTierPage.save();
		return tierName;
	}

	@SuppressWarnings("unused")
	private String createSubscriptionTier()
	{
		return createSubscriptionTier(this.getClass().getSimpleName() + " ray, a glob of hot cross buns");
	}

	/**
	 * create a disposable subscription tier, its name beginning with this
	 * class's simple name
	 * 
	 * @return
	 */
	private String createSubscriptionTier(String name)
	{
		final String tierName = name;
		ShowTiersPage pricingTiersPage = new ShowTiersPage(context).load();
		EditTierPage createSubscriptionTierPage = pricingTiersPage.createTier(true);
		createSubscriptionTierPage.setName(tierName);
		createSubscriptionTierPage.setDescription("and did those feet in ancient time?");
		createSubscriptionTierPage.enablePeriod(4, true).setPrice(4, "65.40");
		pricingTiersPage = createSubscriptionTierPage.save();
		return tierName;
	}

	private PrefixedName createCatalogue(String whiteOrBlackSuffix)
	{
		PrefixedName newCatalogueName = new NotPrefixedName(CATALOGUE_NAME + whiteOrBlackSuffix);
		SettingsPage settingsPage = new SettingsPage(context).load();
		assertTrue(settingsPage.isSettingVisible("Catalogues"));
		ShowCataloguesPage cataloguesPage = settingsPage.showCataloguesPage();
		EditCataloguePage editCat = cataloguesPage.createCatalogue();
		editCat.setName(newCatalogueName);
		editCat.setDescription(CATALOGUE_DESC);
		editCat.setEnabled(true);
		cataloguesPage = editCat.save();
		assertTrue(cataloguesPage.entityExists(newCatalogueName));
		return newCatalogueName;
	}
}
