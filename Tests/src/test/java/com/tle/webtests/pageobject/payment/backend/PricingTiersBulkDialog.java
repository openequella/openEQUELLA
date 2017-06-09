package com.tle.webtests.pageobject.payment.backend;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.generic.component.EquellaSelect;
import com.tle.webtests.pageobject.searching.BulkActionDialog;
import com.tle.webtests.pageobject.searching.BulkResultsPage;

public class PricingTiersBulkDialog extends AbstractPage<PricingTiersBulkDialog>
{
	@FindBy(id = "bcto_fb")
	private WebElement freeCheckbox;
	@FindBy(id = "bcto_ptl")
	private WebElement outrightPurchaseList;
	@FindBy(id = "bcto_stt")
	private WebElement subscriptionTable;

	private BulkActionDialog dialog;
	private EquellaSelect outrightPurchaseSelect;

	public PricingTiersBulkDialog(BulkActionDialog dialog)
	{
		super(dialog.getContext(), By.xpath("//h3[text()='Set pricing tiers']"));
		this.dialog = dialog;
	}

	@Override
	public void checkLoaded() throws Error
	{
		super.checkLoaded();
		outrightPurchaseSelect = new EquellaSelect(dialog.getContext(), outrightPurchaseList);
	}

	public void assignFree()
	{
		freeCheckbox.click();
	}

	public void assignOutrightTier(String tierName)
	{
		outrightPurchaseSelect.selectByVisibleText(tierName);
	}

	public void assignSubscriptionTier(String tierName)
	{
		subscriptionTable.findElement(
			By.xpath("//tbody/tr/td[2][text() = " + quoteXPath(tierName) + "]/../td[1]/input")).click();
	}

	public BulkResultsPage executeBulk()
	{
		dialog.execute();
		return new BulkResultsPage(context).get();
	}
}
