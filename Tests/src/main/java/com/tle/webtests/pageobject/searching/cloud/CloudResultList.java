package com.tle.webtests.pageobject.searching.cloud;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.searching.AbstractResultList;

/**
 * @author Aaron
 */
public class CloudResultList extends AbstractResultList<CloudResultList, CloudSearchResult>
{
	@FindBy(id = "searchresults")
	private WebElement resultDiv;

	public CloudResultList(PageContext context)
	{
		super(context);
	}

	@Override
	public WebElement getResultsDiv()
	{
		return resultDiv;
	}

	@Override
	protected CloudSearchResult createResult(SearchContext relativeTo, By by)
	{
		return new CloudSearchResult(this, relativeTo, by);
	}
}
