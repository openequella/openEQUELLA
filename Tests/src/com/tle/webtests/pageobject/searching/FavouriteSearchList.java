package com.tle.webtests.pageobject.searching;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.framework.PageContext;

public class FavouriteSearchList extends AbstractResultList<FavouriteSearchList, ItemSearchResult>
{

	@FindBy(xpath = "//div[@class='itemlist']")
	private WebElement resultDiv;

	public FavouriteSearchList(PageContext context)
	{
		super(context);
	}

	@Override
	public WebElement getResultsDiv()
	{
		return resultDiv;
	}

	@Override
	public void checkLoaded() throws Error
	{
		if( isPresent(By.xpath("//h3[text()='You have no favourites']"))
			|| isPresent(By.xpath("//h3[text()='No favourites found']")) )
		{
			return; // loaded but no results
		}
		super.checkLoaded();
	}

	@Override
	protected ItemSearchResult createResult(SearchContext context, By by)
	{
		return new ItemSearchResult(this, context, by);
	}
}
