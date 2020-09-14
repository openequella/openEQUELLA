package com.tle.webtests.pageobject.searching;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import java.util.List;
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

  public List<WebElement> getSearchResultItems() {
    return driver.findElements(By.xpath("//ul[@id='search_result_list']//li"));
  }

  public WebElement getItemTitleLink(String title) {
    return driver.findElement(By.linkText(title));
  }
}
