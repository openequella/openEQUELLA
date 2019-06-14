package com.tle.webtests.pageobject.generic.page;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public abstract class AbstractScreenOptions<T extends AbstractScreenOptions<T>>
    extends AbstractPage<T> {

  public AbstractScreenOptions(PageContext context) {
    super(context);
    loadedBy =
        context.getTestConfig().isNewUI() ? By.id("screenOptions") : By.id("bluebar_screenoptions");
  }

  private By getOpenOptionsBy() {
    if (!context.getTestConfig().isNewUI()) {
      return By.id("bluebar_screenoptions_button");
    } else {
      return By.id("screenOptionsOpen");
    }
  }

  private WebElement getOpenOptions() {
    return driver.findElement(getOpenOptionsBy());
  }

  public T open() {
    if (!isPresent(loadedBy)) {
      getOpenOptions().click();
    }
    return get();
  }

  public void close() {
    if (isPresent(loadedBy)) {
      getOpenOptions().click();
    }
  }
}
