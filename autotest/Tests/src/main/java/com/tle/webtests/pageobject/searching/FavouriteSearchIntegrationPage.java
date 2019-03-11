package com.tle.webtests.pageobject.searching;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.WaitingPageObject;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class FavouriteSearchIntegrationPage extends AbstractPage<FavouriteSearchIntegrationPage> {
  @FindBy(id = "afss_nf")
  private WebElement searchNameFiled;

  @FindBy(id = "cfsa_fd_okButton")
  private WebElement addSearchButton;

  public FavouriteSearchIntegrationPage(PageContext context) {
    super(context);
  }

  @Override
  protected WebElement findLoadedElement() {
    return searchNameFiled;
  }

  public <T extends AbstractPage<T>> T addSearchToFavourite(
      String name, WaitingPageObject<T> page) {
    waitForElement(searchNameFiled);
    searchNameFiled.sendKeys(name);
    addSearchButton.click();
    return page.get();
  }
}
