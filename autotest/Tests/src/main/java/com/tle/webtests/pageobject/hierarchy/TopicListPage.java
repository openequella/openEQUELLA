package com.tle.webtests.pageobject.hierarchy;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.searching.AbstractItemList;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;

public class TopicListPage extends AbstractItemList<TopicListPage, TopicSearchResult> {
  public TopicListPage(PageContext context) {
    super(context);
  }

  @Override
  protected TopicSearchResult createResult(SearchContext searchContext, By by) {
    return new TopicSearchResult(this, searchContext, by);
  }

  public boolean doesKeyResourceExist(String title, int index) {
    return isPresent(getByForKeyResource(title, index));
  }

  protected static String getXpathForKeyResource(String title) {
    return "//div[@class='itemresult hilighted' and .//h3/a[normalize-space(string())="
        + quoteXPath(title)
        + "]]";
  }

  protected static By getByForKeyResource(String title, int index) {
    return By.xpath(getXpathForKeyResource(title) + "[" + index + "]");
  }
}
