package com.tle.webtests.pageobject.searching;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.framework.PageContext;

public class FilterSearchResultsPage
	extends
		AbstractQueryableSearchPage<FilterSearchResultsPage, ItemListPage, ItemSearchResult>
{
	@FindBy(id = "searchresults")
	private WebElement mainElem;

	public FilterSearchResultsPage(PageContext context)
	{
		super(context);
	}

	@Override
	protected WebElement findLoadedElement()
	{
		return mainElem;
	}

	@Override
	public ItemListPage resultsPageObject()
	{
		return new ItemListPage(context);
	}
}
