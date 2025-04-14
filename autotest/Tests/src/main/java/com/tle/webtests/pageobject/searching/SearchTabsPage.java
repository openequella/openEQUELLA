package com.tle.webtests.pageobject.searching;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class SearchTabsPage extends AbstractPage<SearchTabsPage> {
  public SearchTabsPage(PageContext context) {
    super(context, context.getDriver(), By.id("searchtabs"));
  }

  public List<WebElement> getTabs() {
    return getLoadedElement().findElements(By.xpath("//li/a"));
  }

  public boolean hasTab(String tabType) {
    return isPresent(loadedBy) ? getTab(tabType) != null : false;
  }

  public <P extends AbstractQueryableSearchPage<?, ?, ?>> P clickTab(String tabType, P targetPage) {
    WebElement tab = getTab(tabType);
    if (tab == null) {
      throw new RuntimeException("No search tab with type " + tabType);
    }
    tab.click();
    return (P) targetPage.get();
  }

  /**
   * @param tabType standard or cloud
   * @return
   */
  protected WebElement getTab(String tabType) {
    checkLoadedElement();
    return elementIfPresent(
        getLoadedElement(), By.xpath("//a[@class=" + quoteXPath(tabType) + "]"));
  }
}
