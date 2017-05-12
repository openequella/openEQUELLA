package com.tle.webtests.pageobject.portal;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.searching.SearchPage;

public class SearchPortalSection extends AbstractPortalSection<SearchPortalSection>
{
	public SearchPortalSection(PageContext context, String title)
	{
		super(context, title);
	}

	public SearchPage search(String query)
	{
		setQuery(query);
		return search();
	}

	public SearchPage search()
	{
		boxContent.findElement(By.xpath("div/div/button")).click();
		return new SearchPage(context).get();
	}

	public void setQuery(String query)
	{
		WebElement queryField = boxContent.findElement(By.xpath("div/div/div/input"));
		queryField.clear();
		queryField.sendKeys(query);
	}
}
