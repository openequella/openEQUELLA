package com.tle.webtests.pageobject.payment.storefront;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.searching.AbstractResultList;

public class CatalogueSearchList extends AbstractResultList<CatalogueSearchList, CatalogueSearchResult>
{
	@FindBy(id = "searchresults")
	private WebElement resultDiv;

	public CatalogueSearchList(PageContext context)
	{
		super(context);
	}

	@Override
	public WebElement getResultsDiv()
	{
		return resultDiv;
	}

	@Override
	protected CatalogueSearchResult createResult(SearchContext searchContext, By by)
	{
		return new CatalogueSearchResult(this, searchContext, by);
	}

	public boolean doesResultHaveThumbnail(String title)
	{
		return isPresent(By.xpath(getXPathForTitle(title) + "//img"));
	}
}
