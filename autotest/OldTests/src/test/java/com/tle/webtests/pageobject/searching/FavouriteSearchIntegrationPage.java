package com.tle.webtests.pageobject.searching;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.WaitingPageObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

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
    By successMessageBy =
        By.xpath("//span[text()='Successfully added this search to your favourites']");

    waitForElement(searchNameFiled);
    searchNameFiled.sendKeys(name);
    addSearchButton.click();
    waiter.until(ExpectedConditions.visibilityOfElementLocated(successMessageBy));

    return page.get();
  }
}
