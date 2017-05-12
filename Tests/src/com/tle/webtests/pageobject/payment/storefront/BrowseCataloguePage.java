package com.tle.webtests.pageobject.payment.storefront;

import java.util.TimeZone;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.generic.component.EquellaSelect;
import com.tle.webtests.pageobject.searching.AbstractQueryableSearchPage;
import com.tle.webtests.pageobject.searching.AbstractSearchPage;

public class BrowseCataloguePage
	extends
		AbstractQueryableSearchPage<BrowseCataloguePage, CatalogueSearchList, CatalogueSearchResult>
{
	@FindBy(id = "ssfbdr_clearButton")
	private WebElement clearDate;
	@FindBy(id = "ssfbpm_clearButton")
	private WebElement clearPrice;
	@FindBy(xpath = "//div[@id='col1']/div/div[2]/h2[text() = {storeNameXpath}]/../..//h4[contains(text(),{catalogueXpath})]")
	private WebElement titleElem;

	private String storeName;
	private String catalogue;

	private static final TimeZone USERS_TIMEZONE = TimeZone.getTimeZone("America/Chicago");

	public BrowseCataloguePage(PageContext context, String storeName, String catalogue)
	{
		super(context);
		this.storeName = storeName;
		this.catalogue = catalogue;
	}

	@Override
	protected WebElement findLoadedElement()
	{
		return titleElem;
	}

	public String getStoreNameXpath()
	{
		return quoteXPath(storeName);
	}

	public String getCatalogueXpath()
	{
		return quoteXPath(catalogue);
	}

	@Override
	public void checkLoaded() throws Error
	{
		// Stores with single catalogues go straight to it, dogical hax
		try
		{
			driver.findElement(
				By.xpath("// div[@id = 'catalogue_list']/ul/li/a[contains(.," + quoteXPath(catalogue) + ")]")).click();
		}
		catch( Throwable e )
		{
			// ignore
		}
		super.checkLoaded();
	}

	public boolean checkStoreIcon(String backendInstitution)
	{
		WebElement image = driver.findElement(By.xpath("//div[@id='col1']/div/div[@class='topright']/img"));
		String source = image.getAttribute("src");
		String restSource = "/api/store/icon/small.jpg";
		if( source.contains(restSource) )
		{
			return true;
		}

		return false;
	}

	@Override
	public CatalogueSearchList resultsPageObject()
	{
		return new CatalogueSearchList(context);
	}

	public CatalogueSearchList search()
	{
		return querySection.search(resultsPageObject.getUpdateWaiter());
	}

	public String getStoreName()
	{
		WebElement header = driver.findElement(By.xpath("//div[@id='col1']/div/div[2]/h2"));
		return header.getText();
	}

	public String getCatalogueName()
	{
		WebElement catName = driver.findElement(By.xpath("//div[@id='col1']/div/div[@class='indent']/h4"));
		return catName.getText();
	}

	public BrowseCataloguePage clearDateFilter()
	{
		WaitingPageObject<CatalogueSearchList> waiter = resultsPageObject.getUpdateWaiter();
		clearDate.click();
		return waitForResultsReload(waiter);
	}

	public BrowseCataloguePage clearPriceFilter()
	{
		WaitingPageObject<CatalogueSearchList> waiter = resultsPageObject.getUpdateWaiter();
		clearPrice.click();
		return waitForResultsReload(waiter);
	}

	public BrowseCataloguePage reverseSort()
	{
		WaitingPageObject<CatalogueSearchList> waiter = resultsPageObject.getUpdateWaiter();
		WebElement reverseSort = getCollapsibleControl("rs", AbstractSearchPage.SORT_BUTTON_ID);
		reverseSort.click();
		return waitForResultsReload(waiter);
	}

	public BrowseCataloguePage setPriceFilter(String type)
	{
		WaitingPageObject<CatalogueSearchList> waiter = resultsPageObject.getUpdateWaiter();
		WebElement priceFilter = getCollapsibleControl("price", getFilterOpenerId());
		new EquellaSelect(context, priceFilter).selectByValue(type);
		return waitForResultsReload(waiter);
	}
}
