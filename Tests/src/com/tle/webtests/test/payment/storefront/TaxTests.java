package com.tle.webtests.test.payment.storefront;

import java.math.BigDecimal;
import java.text.NumberFormat;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.tle.webtests.framework.Name;
import com.tle.webtests.framework.PageContext;
import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.NotPrefixedName;
import com.tle.webtests.pageobject.PrefixedName;
import com.tle.webtests.pageobject.payment.backend.EditStoreFrontRegistrationPage;
import com.tle.webtests.pageobject.payment.backend.EditTaxPage;
import com.tle.webtests.pageobject.payment.backend.ItemCataloguesPage;
import com.tle.webtests.pageobject.payment.backend.ItemPricingTiersPage;
import com.tle.webtests.pageobject.payment.backend.ShowStoreFrontRegistrationsPage;
import com.tle.webtests.pageobject.payment.backend.ShowTaxesPage;
import com.tle.webtests.pageobject.payment.storefront.BrowseCataloguePage;
import com.tle.webtests.pageobject.payment.storefront.CartViewPage;
import com.tle.webtests.pageobject.payment.storefront.CatalogueResourcePage;
import com.tle.webtests.pageobject.payment.storefront.ShopPage;
import com.tle.webtests.pageobject.payment.storefront.StoreFrontSettingsPage;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.test.AbstractSessionTest;
import com.tle.webtests.test.files.Attachments;
import com.tle.webtests.test.payment.global.RegisterStoreAndStoreFront;

/**
 * Note: uses storebackendssl as the store with taxes configured
 * 
 * @author Aaron
 */
@TestInstitution("taxedstorefront")
public class TaxTests extends AbstractSessionTest
{
	private static final PrefixedName CATALOGUE = new NotPrefixedName("cat1");

	@Name(value = "Veronicas", group = "items")
	private static PrefixedName ITEM_NAME_1;
	@Name(value = "Page HTML", group = "items")
	private static PrefixedName ITEM_NAME_2;

	@Name(value = "High tax, lots of decimals '''", group = "taxes")
	private static PrefixedName TAX_NAME_1;
	private static final String TAX_CODE_1 = "HIGH_TAX";
	private static final BigDecimal TAX_RATE_1 = new BigDecimal("999.0012");

	@Name(value = "Normal tax, seven percent", group = "taxes")
	private static PrefixedName TAX_NAME_2;
	private static final String TAX_CODE_2 = "NORM_TAX";
	private static final BigDecimal TAX_RATE_2 = new BigDecimal("7.0");

	private static final BigDecimal PURCHASE_TOTAL = new BigDecimal("5.0");
	private static final BigDecimal SUBSCRIPTION_WEEK_TOTAL = new BigDecimal("25.0");

	private static final String STORE_NAME = RegisterStoreAndStoreFront.STORE_NAME;

	@Test
	public void makeTaxes()
	{
		PageContext context2 = newContext(RegisterStoreAndStoreFront.INSTITUTION_STORE);

		logon(context2, "AutoTest", "automated");
		ShowTaxesPage showTaxes = new ShowTaxesPage(context2).load();

		EditTaxPage editTax = showTaxes.createTax();
		editTax.setName(TAX_NAME_1);
		editTax = editTax.saveWithErrors();
		Assert.assertEquals(editTax.getCodeValidationMessage(), EditTaxPage.VALIDATION_MISSING_CODE,
			"Code was not invalid");
		Assert.assertEquals(editTax.getRateValidationMessage(), EditTaxPage.VALIDATION_MISSING_RATE,
			"Rate was not invalid");

		// add code
		editTax.setCode(TAX_CODE_1);
		editTax = editTax.saveWithErrors();
		Assert.assertEquals(editTax.getRateValidationMessage(), EditTaxPage.VALIDATION_MISSING_RATE,
			"Rate was not invalid");

		// too big code, add valid percent
		editTax.setCode("AAAAAAAAAAAAAAAAAAAAAA");
		editTax.setRate(new BigDecimal(10.00));
		editTax = editTax.saveWithErrors();
		Assert.assertEquals(editTax.getCodeValidationMessage(), EditTaxPage.VALIDATION_MASSIVE_CODE,
			"Code was not invalid");
		Assert.assertEquals(editTax.getRateValidationMessage(), null, "Rate was not valid");

		// add code, change percent to non-numeric
		editTax.setCode(TAX_CODE_1);
		editTax.setRate("@#$%^&*");
		editTax = editTax.saveWithErrors();
		Assert.assertEquals(editTax.getCodeValidationMessage(), null, "Code was not valid");

		// Chrome doesnt let you set a number input to text
		if( context.getTestConfig().isChromeDriverSet() )
		{
			Assert.assertEquals(editTax.getRateValidationMessage(), EditTaxPage.VALIDATION_MISSING_RATE,
				"Rate was not invalid");
		}
		else
		{
			Assert.assertEquals(editTax.getRateValidationMessage(), EditTaxPage.VALIDATION_NON_NUMERIC_RATE,
				"Rate was not invalid");
		}

		// change percent to too high
		editTax.setRate("10000000.00");
		editTax = editTax.saveWithErrors();
		Assert.assertEquals(editTax.getCodeValidationMessage(), null, "Code was not valid");
		Assert.assertEquals(editTax.getRateValidationMessage(), EditTaxPage.VALIDATION_MASSIVE_RATE,
			"Rate was not invalid");

		// change percent to too many decimals
		editTax.setRate("1.00001");
		editTax = editTax.saveWithErrors();
		Assert.assertEquals(editTax.getCodeValidationMessage(), null, "Code was not valid");
		Assert.assertEquals(editTax.getRateValidationMessage(), EditTaxPage.VALIDATION_TRUNCATED_RATE,
			"Rate was not invalid");

		// change percent to negative
		editTax.setRate("-3.0");
		editTax = editTax.saveWithErrors();
		Assert.assertEquals(editTax.getCodeValidationMessage(), null, "Code was not valid");
		Assert.assertEquals(editTax.getRateValidationMessage(), EditTaxPage.VALIDATION_NEGATIVE_RATE,
			"Rate was not invalid");

		// change percent to 1000.0012
		editTax.setRate(TAX_RATE_1);
		showTaxes = editTax.save();

		// clone the tax
		editTax = showTaxes.cloneTax(TAX_NAME_1);
		// ensure cloned values
		Assert.assertEquals(editTax.getCode(), TAX_CODE_1, "Tax code was not cloned");
		Assert.assertTrue(editTax.getRateBigDecimal().compareTo(TAX_RATE_1) == 0, "Tax rate was not cloned");

		// set new (reasonable) values
		editTax.setName(TAX_NAME_2);
		editTax.setCode(TAX_CODE_2);
		editTax.setRate(TAX_RATE_2);
		showTaxes = editTax.save();

		// view original tax and ensure original values
		editTax = showTaxes.editTax(TAX_NAME_1);
		Assert.assertEquals(editTax.getCode(), TAX_CODE_1, "Tax code ruined by clone");
		Assert.assertTrue(editTax.getRateBigDecimal().compareTo(TAX_RATE_1) == 0, "Tax rate ruined by cloned");

		// assign the (1st) tax to taxedstorefront
		ShowStoreFrontRegistrationsPage showStorefronts = new ShowStoreFrontRegistrationsPage(context2).load();
		EditStoreFrontRegistrationPage editStorefront = showStorefronts
			.editRegistration(RegisterStoreAndStoreFront.STOREFRONT_NAME_TAXED);
		editStorefront.setTax(TAX_NAME_1);
		showStorefronts = editStorefront.save();

		// Edit the storefront again, ensure tax selection was maintained
		editStorefront = showStorefronts.editRegistration(RegisterStoreAndStoreFront.STOREFRONT_NAME_TAXED);
		Assert.assertEquals(editStorefront.getSelectedTax(), TAX_NAME_1.toString(), "Tax selection was not maintained");
		editStorefront.cancel();

		// Contribute some items we can check the prices of
		ContributePage contributePage = new ContributePage(context2).load();
		WizardPageTab wizardPage = contributePage.openWizard("Preview collection");
		wizardPage.editbox(1, ITEM_NAME_1);
		wizardPage.addFile(3, Attachments.get("veronicas_wall1.jpg"), true);
		ItemCataloguesPage cats = wizardPage.save().publish().editCataloguesPage();
		cats.addToWhiteList(CATALOGUE);

		ItemPricingTiersPage tiers = cats.summary().editPricingTiersPage();
		// Value = $5
		tiers.selectOutrightPricingTier("Outright Purchase $5.00 AUD");
		tiers.save();

		contributePage = new ContributePage(context2).load();
		wizardPage = contributePage.openWizard("Preview collection");
		wizardPage.editbox(1, ITEM_NAME_2);
		wizardPage.addFile(3, Attachments.get("page.html"), true);
		cats = wizardPage.save().publish().editCataloguesPage();
		cats.addToWhiteList(CATALOGUE);

		tiers = cats.summary().editPricingTiersPage();
		// Week = $25
		tiers.getSubscriptionTier("Subscription").select();
		tiers.save();
	}

	@Test(dependsOnMethods = "makeTaxes")
	public void testStoreFrontIncludesTax()
	{

		logon(context, "AutoTest", "automated");

		StoreFrontSettingsPage sfs = new StoreFrontSettingsPage(context).load();
		sfs.setCollection("Basic Items");
		sfs.setShowTax(true);
		sfs.saveAndCheckReceipt();

		// Go shopping
		ShopPage shops = new ShopPage(context).load();
		BrowseCataloguePage search = shops.pickStoreSingleCatalogue(STORE_NAME, CATALOGUE);
		CatalogueResourcePage summary = search.search(ITEM_NAME_1).getResultForTitle(ITEM_NAME_1).viewSummary();
		summary.setNumberOfUsers(false, "1");

		BigDecimal ratePc = TAX_RATE_1.movePointLeft(2);
		BigDecimal taxedValue = PURCHASE_TOTAL.multiply(ratePc).add(PURCHASE_TOTAL);
		final NumberFormat df = NumberFormat.getNumberInstance();
		df.setMinimumFractionDigits(2);
		df.setMaximumFractionDigits(2);
		String formattedTaxedValue = "$" + df.format(taxedValue) + " AUD";

		Assert.assertEquals(summary.getTotal(), formattedTaxedValue);
		// add to cart
		summary.addToCart();
		CartViewPage cart = summary.viewCart();
		// check value in cart table
		Assert.assertEquals(cart.getTopTotal(), formattedTaxedValue);
		Assert.assertEquals(cart.getBottomTotal(), formattedTaxedValue);

		// Assert.assertEquals(cart.getStoreSubTotal(RegisterStoreAndStoreFront.STORE_NAME),
		// formattedTaxedValue);
		// check cart total

	}

	@Test(dependsOnMethods = "makeTaxes")
	public void testStoreFrontExcludesTax()
	{

	}

	@Override
	protected void cleanupAfterClass() throws Exception
	{
		PageContext context2 = newContext(RegisterStoreAndStoreFront.INSTITUTION_STORE);

		logon(context2, "AutoTest", "automated");

		// unassign the tax
		ShowStoreFrontRegistrationsPage showStorefronts = new ShowStoreFrontRegistrationsPage(context2).load();
		EditStoreFrontRegistrationPage editStorefront = showStorefronts
			.editRegistration(RegisterStoreAndStoreFront.STOREFRONT_NAME_TAXED);
		editStorefront.setTax(null);
		showStorefronts = editStorefront.save();

		ShowTaxesPage taxes = new ShowTaxesPage(context2).load();
		taxes.deleteAllNamed(getNames("taxes"));

		logon("AutoTest", "automated");
		StoreFrontSettingsPage sfs = new StoreFrontSettingsPage(context).load();
		if( !"<None>".equals(sfs.getCollection()) )
		{
			sfs.setCollection("<None>");
			sfs.saveAndCheckReceipt();
		}
	}
}
