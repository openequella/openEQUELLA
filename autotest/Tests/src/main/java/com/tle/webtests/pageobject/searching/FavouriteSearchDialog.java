package com.tle.webtests.pageobject.searching;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.PageObject;
import com.tle.webtests.pageobject.WaitingPageObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedCondition;

public class FavouriteSearchDialog extends AbstractPage<FavouriteSearchDialog> {
  private final String prefix;

  @FindBy(id = "afss_nf")
  private WebElement nameField;

  public FavouriteSearchDialog(PageContext context, String prefix) {
    super(context, By.id(prefix + "_fd"));
    this.prefix = prefix + "_fd";
  }

  private WebElement getOpener() {
    return driver.findElement(By.id(prefix + "_opener"));
  }

  private WebElement getSaveButton() {
    return driver.findElement(By.id(prefix + "_okButton"));
  }

  public String getPrefix() {
    return prefix;
  }

  public FavouriteSearchDialog open() {
    getOpener().click();
    return get();
  }

  public <T extends PageObject> T favourite(String searchName, WaitingPageObject<T> targetPage) {
    nameField.sendKeys(searchName);
    ExpectedCondition<Boolean> removalCondition = removalCondition(nameField);
    getSaveButton().click();
    getWaiter().until(removalCondition);
    return targetPage.get();
  }
}
