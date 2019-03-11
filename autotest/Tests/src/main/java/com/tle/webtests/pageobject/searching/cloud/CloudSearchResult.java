package com.tle.webtests.pageobject.searching.cloud;

import com.tle.webtests.pageobject.searching.SearchResult;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;

public class CloudSearchResult extends SearchResult<CloudSearchResult> {
  public CloudSearchResult(CloudResultList resultPage, SearchContext searchContext, By by) {
    super(resultPage, searchContext, by);
  }
}
