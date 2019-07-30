package com.tle.webtests.pageobject.generic.page;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;

public abstract class AbstractScreenOptions<T extends AbstractScreenOptions<T>>
    extends AbstractPage<T> {

  public AbstractScreenOptions(PageContext context) {
    super(context);
    loadedBy = By.id(getOptionsId());
  }

  protected String getOptionsId() {
    return isNewUI() ? "screenOptions" : "bluebar_screenoptions";
  }

  private By getOpenOptionsBy() {
    if (!isNewUI()) {
      return By.id("bluebar_screenoptions_button");
    } else {
      return By.id("screenOptionsOpen");
    }
  }

  private WebElement getOpenOptions() {
    return driver.findElement(getOpenOptionsBy());
  }

  public T open() {
    if (!isVisible(loadedBy)) {
      getOpenOptions().click();
    }
    return get();
  }

  public T close() {
    if (isNewUI()) {
      Actions action = new Actions(driver);
      action.sendKeys(Keys.ESCAPE).build().perform();
      waiter.until(ExpectedConditions.invisibilityOfElementLocated(loadedBy));
      action.click();
      return actualPage();
    } else {
      if (!isPresent(loadedBy)) {
        getOpenOptions().click();
      }
      return get();
    }
  }
}
