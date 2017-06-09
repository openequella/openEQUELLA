package com.tle.webtests.pageobject.payment.storefront;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;

import com.tle.webtests.pageobject.searching.AbstractResultList;
import com.tle.webtests.pageobject.searching.SearchResult;

public class CatalogueSearchResult extends SearchResult<CatalogueSearchResult>
{
	public CatalogueSearchResult(AbstractResultList<?, ?> page, SearchContext relativeTo, By by)
	{
		super(page, relativeTo, by);
	}

	public String getDescription()
	{
		return resultDiv.findElement(By.xpath(".//p[1]")).getText();
	}

	public CatalogueResourcePage viewSummary()
	{
		clickTitle();
		return new CatalogueResourcePage(context).get();
	}
}
