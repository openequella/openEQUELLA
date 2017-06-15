package com.tle.webtests.pageobject.searching.cloud;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;

import com.tle.webtests.pageobject.searching.SearchResult;

public class CloudSearchResult extends SearchResult<CloudSearchResult>
{
	public CloudSearchResult(CloudResultList resultPage, SearchContext searchContext, By by)
	{
		super(resultPage, searchContext, by);
	}
}
