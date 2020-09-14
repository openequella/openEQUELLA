package com.tle.webtests.pageobject.searching;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class NewSearchPage extends AbstractPage<NewSearchPage> {

  public NewSearchPage(PageContext context) {
    super(context);
  }

  @Override
  protected void loadUrl() {
    driver.get(context.getBaseUrl() + "page/search");
  }

  @Override
  protected WebElement findLoadedElement() {
    // When the Search bar is visible, the page is loaded.
    return getSearchBar();
  }

  public WebElement getSearchBar() {
    return driver.findElement(By.id("searchBar"));
  }
}
