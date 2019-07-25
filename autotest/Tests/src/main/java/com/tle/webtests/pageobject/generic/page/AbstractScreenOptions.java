package com.tle.webtests.pageobject.generic.page;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
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

  protected boolean isOptionsOpen() {
    return isVisible(loadedBy);
  }

  public T open() {
    if (!isOptionsOpen()) {
      ExpectedCondition<WebElement> visible =
          ExpectedConditions.visibilityOfElementLocated(loadedBy);
      getOpenOptions().click();
      waiter.until(visible);
    }
    return get();
  }

  public void close() {
    if (isOptionsOpen()) {
      ExpectedCondition<Boolean> invisible =
          ExpectedConditions.invisibilityOfElementLocated(loadedBy);
      getOpenOptions().click();
      waiter.until(invisible);
    }
  }

  public void ensureOptionsGone() {
    if (isNewUI()) {
      driver.findElement(By.xpath("//body")).click();
    }
  }
}
