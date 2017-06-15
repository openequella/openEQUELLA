package com.tle.webtests.pageobject;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.framework.PageContext;

public class LicencesPage extends AbstractPage<LicencesPage>
{
	@FindBy(id="l_l")
	private WebElement licenceTable;
	
	public LicencesPage(PageContext context)
	{
		super(context);
	}

	@Override
	protected void loadUrl()
	{
		driver.get(context.getBaseUrl() + "licences.do");
	}

	@Override
	protected WebElement findLoadedElement()
	{
		return licenceTable;
	}

	public boolean doesLicenceExists(String vendor)
	{
		return isPresent(licenceTable, By.xpath("./tbody/tr/td[text() = " + quoteXPath(vendor) + "]"));
	}

	public int getLicenceCount()
	{

		return licenceTable.findElements(By.xpath("./tbody/tr")).size();
	}

}
