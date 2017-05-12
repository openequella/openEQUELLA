package com.tle.webtests.pageobject.payment.backend;

import static org.testng.Assert.assertEquals;

import org.openqa.selenium.Alert;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.testng.Assert;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.PrefixedName;
import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.viewitem.ItemPage;

/**
 * @author Dinuk
 */
public class ItemCataloguesPage extends ItemPage<ItemCataloguesPage>
{
	private static String ALERT_DYNAMIC_CATALOGUE_ACTION_EXCLUDE = "Are you sure you want to exclude this resource from the catalogue?";
	private static String ALERT_REMOVE_FROM_MANUAL_ADDITION = "Are you sure you want remove this resource from the catalogue's manual additions?";
	private static String ALERT_ADD_TO_CATALOGUE = "Are you sure you want to manually add this resource to the catalogue?";

	// Table of th UI
	@FindBy(id = "arfc_currentCatalogues")
	private WebElement table;
	@FindBy(xpath = "//h2[normalize-space(text())='Edit catalogues']")
	private WebElement catalogueTitle;

	// Define EditCatalogues Constructor
	public ItemCataloguesPage(PageContext context)
	{
		super(context);
	}

	@Override
	protected WebElement findLoadedElement()
	{
		return catalogueTitle;
	}

	// Verify 'Add to catalogue' Action link display
	public boolean canAdd(PrefixedName name)
	{
		return getCatalogueRow(name).isAddAvailable();
	}

	// Verify 'Remove from manual additions' Action link display
	public boolean canRemove(PrefixedName name)
	{
		return getCatalogueRow(name).isRemoveManualAvailable();
	}

	private EditCataloguesRow getCatalogueRow(PrefixedName name)
	{
		return new EditCataloguesRow(this, table, name.toString()).get();
	}

	// Change Add to Catalogue Action and Verify Alert Message
	public ItemCataloguesPage addToWhiteList(PrefixedName name)
	{
		EditCataloguesRow row = getCatalogueRow(name);
		WaitingPageObject<ItemCataloguesPage> updateWaiter = ajaxUpdateExpect(table, row.getLoadedElement());
		Alert alert = row.clickAdd();
		assertEquals(alert.getText(), ALERT_ADD_TO_CATALOGUE);
		alert.accept();
		return updateWaiter.get();
	}

	// Change Add to Catalogue Action and Verify Alert Message
	public ItemCataloguesPage removeFromWhiteList(PrefixedName name)
	{
		EditCataloguesRow row = getCatalogueRow(name);
		WaitingPageObject<ItemCataloguesPage> updateWaiter = ajaxUpdateExpect(table, row.getLoadedElement());
		Alert alert = row.clickRemoveWhiteList();
		Assert.assertEquals(alert.getText(), ALERT_REMOVE_FROM_MANUAL_ADDITION);
		alert.accept();
		return updateWaiter.get();
	}

	// Verify 'Exclude from catalogue' Action link display - Dinuk
	public boolean canExclude(PrefixedName name)
	{
		return getCatalogueRow(name).isExcludeAvailable();
	}

	// Verify 'Remove from exclusions' Action link display - Dinuk
	public boolean canUnexclude(PrefixedName name)
	{
		return getCatalogueRow(name).isRemoveExcludeAvailable();
	}

	// Verify Catalogue Status Display - Dinuk
	public String catalogueStatus(PrefixedName name)
	{
		return getCatalogueRow(name).getStatus();
	}

	// Verify Catalogue isPresent - Dinuk
	public boolean catalogueExists(PrefixedName catalogueName)
	{
		return new EditCataloguesRow(this, table, catalogueName.toString()).isLoaded();
	}

	// Change Add to Catalogue Action and Verify Alert Message
	public ItemCataloguesPage exclude(PrefixedName name)
	{
		EditCataloguesRow row = getCatalogueRow(name);
		WaitingPageObject<ItemCataloguesPage> updateWaiter = ajaxUpdateExpect(table, row.getLoadedElement());
		Alert alert = row.clickExclude();
		Assert.assertEquals(alert.getText(), ALERT_DYNAMIC_CATALOGUE_ACTION_EXCLUDE);
		alert.accept();
		return updateWaiter.get();
	}

	// Verify Catalogue Live Status Display - Dinuk
	public boolean isIncluded(PrefixedName catalogue)
	{
		EditCataloguesRow cr = getCatalogueRow(catalogue);
		return cr.isExcludeAvailable() || cr.isRemoveManualAvailable();
	}

	public boolean isExcluded(PrefixedName catalogue)
	{
		EditCataloguesRow cr = getCatalogueRow(catalogue);
		return cr.isAddAvailable() || cr.isRemoveExcludeAvailable();
	}
}
