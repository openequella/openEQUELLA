package com.tle.webtests.pageobject.payment.storefront;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.generic.component.CheckList;
import com.tle.webtests.pageobject.generic.component.EquellaSelect;

public class StoreFrontSettingsPage extends AbstractPage<StoreFrontSettingsPage>
{
	@FindBy(id = "_cl")
	private WebElement collectionSelect;
	@FindBy(id = "_saveButton")
	private WebElement saveButton;

	public StoreFrontSettingsPage(PageContext context)
	{
		super(context, By.xpath("//div[@class='area']/h2[text() = 'Store front setup']"));
	}

	@Override
	protected void loadUrl()
	{
		driver.get(context.getBaseUrl() + "access/storefrontsettings.do");
	}

	public StoreFrontSettingsPage setCollection(String collectionName)
	{
		EquellaSelect dropdown = new EquellaSelect(context, collectionSelect);
		dropdown.selectByVisibleText(collectionName);
		return this;
	}

	public String getCollection()
	{
		EquellaSelect dropdown = new EquellaSelect(context, collectionSelect);
		return dropdown.getSelectedText();
	}

	public StoreFrontSettingsPage setShowTax(boolean show)
	{
		new CheckList(context, "_it").setSelectionByText(show ? "Yes" : "No");
		return this;
	}

	public boolean isShowTax()
	{
		final List<String> selectedTexts = new CheckList(context, "_it").getSelectedTexts();
		// Shouldn't be possible to select both anyway!
		return selectedTexts.contains("Yes") && !selectedTexts.contains("No");
	}

	public boolean saveAndCheckReceipt()
	{
		saveButton.click();
		try
		{
			driver.switchTo().alert().accept();
		}
		catch( NoAlertPresentException e )
		{
			// carry on
		}
		waitForElement(By.xpath("//div[@id='receipt-message']"));
		return driver.findElement(By.xpath("//div[@id='receipt-message']")).getText()
			.equals("Settings saved successfully");
	}
}
