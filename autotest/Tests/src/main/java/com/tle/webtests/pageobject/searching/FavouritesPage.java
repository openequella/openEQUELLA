package com.tle.webtests.pageobject.searching;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class FavouritesPage extends AbstractPage<FavouritesPage> {
  @FindBy(id = "searchresults-select")
  private WebElement mainElem;

  public FavouritesPage(PageContext context) {
    super(context);
  }

  @Override
  public WebElement findLoadedElement() {
    return mainElem;
  }

  @Override
  protected void loadUrl() {
    driver.get(context.getBaseUrl() + "access/favourites.do");
  }

  @Override
  public void checkLoaded() throws Error {
    List<WebElement> loadingImage = driver.findElements(By.xpath("id('searchresults')/img"));

    if (!loadingImage.isEmpty()) {
      throw new Error("Still ajax loading");
    }
    super.checkLoaded();
  }
  ;

  By xpathForPage(String searchType) {
    return By.xpath(
        "//div[@id='searchresults-select']/strong[text()=" + quoteXPath(searchType) + "]");
  }

  void clickType(String searchType) {
    if (!isPresent(xpathForPage(searchType))) {
      driver
          .findElement(
              By.xpath(
                  "//div[@id='searchresults-select']/a[text()=" + quoteXPath(searchType) + "]"))
          .click();
    }
  }

  public FavouriteSearchesPage searches() {
    return new FavouriteSearchesPage(context, this).loadIfRequired();
  }

  public FavouriteItemsPage items() {
    return new FavouriteItemsPage(context, this).loadIfRequired();
  }

  // Access a Saved Search
  public void accessSavedSearches(String savedSearchName) {
    driver
        .findElement(
            By.xpath(
                "//h3[contains(@class,'itemresult-title')]/a[text()='" + savedSearchName + "']"))
        .click();
  }
}
