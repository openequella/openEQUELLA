package com.tle.webtests.pageobject.portal;

import java.text.MessageFormat;

import org.openqa.selenium.By;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.searching.SearchPage;
import com.tle.webtests.pageobject.viewitem.SummaryPage;

public class FavPortalSection extends AbstractPortalSection<FavPortalSection>
{
	public FavPortalSection(PageContext context, String title)
	{
		super(context, title);
	}

	public boolean favouriteExists(String itemTitle)
	{
		return isPresent(
			boxContent,
			By.xpath(".//div[normalize-space(@class)='alt-links']/a[normalize-space(text())=" + quoteXPath(itemTitle)
				+ "]"));
	}

	public SummaryPage clickFavourite(String itemTitle)
	{
		boxContent.findElement(
			By.xpath(".//div[normalize-space(@class)='alt-links']/a[normalize-space(text())=" + quoteXPath(itemTitle)
				+ "]")).click();
		return new SummaryPage(context).get();
	}

	public boolean favouriteSearchExists(String searchTitle)
	{
		return isPresent(
			boxContent,
			By.xpath(".//div[normalize-space(@class)='alt-links']/a[normalize-space(text())="
				+ quoteXPath(MessageFormat.format("Search results for \"{0}\"", searchTitle)) + "]"));
	}

	public SearchPage clickFavouriteSearch(String searchTitle)
	{
		boxContent.findElement(
			By.xpath(".//div[normalize-space(@class)='alt-links']/a[normalize-space(text())="
				+ quoteXPath(MessageFormat.format("Search results for \"{0}\"", searchTitle)) + "]")).click();
		return new SearchPage(context).get();
	}
}
