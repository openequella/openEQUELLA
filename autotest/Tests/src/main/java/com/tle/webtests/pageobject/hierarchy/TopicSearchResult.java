package com.tle.webtests.pageobject.hierarchy;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.searching.AbstractItemSearchResult;
import com.tle.webtests.pageobject.searching.AbstractResultList;

public class TopicSearchResult extends AbstractItemSearchResult<TopicSearchResult>
{

	public TopicSearchResult(AbstractResultList<?, ?> page, SearchContext relativeTo, By by)
	{
		super(page, relativeTo, by);
	}

	public TopicSearchResult addAsKeyResource()
	{
		WebElement element = resultDiv.findElement(By.xpath(".//a[@title='Add as key resource']"));
		WaitingPageObject<TopicSearchResult> disappearWaiter = removalWaiter(element);
		element.click();
		return disappearWaiter.get();

	}

	public TopicSearchResult removeKeyResource()
	{
		WebElement element = resultDiv.findElement(By.xpath(".//a[@title='Remove key resource']"));
		WaitingPageObject<TopicSearchResult> disappearWaiter = removalWaiter(element);
		element.click();
		return disappearWaiter.get();
	}
}
