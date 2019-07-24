package com.tle.webtests.pageobject.generic.page;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

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

  protected boolean isOptionsOpen() {
    return isVisible(loadedBy);
  }

  public T open() {
    if (!isOptionsOpen()) {
      getOpenOptions().click();
    }
    return get();
  }

  public void close() {
    if (isOptionsOpen()) {
      getOpenOptions().click();
    }
  }
}
