package com.tle.webtests.pageobject.generic.page;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;

public abstract class AbstractScreenOptions<T extends AbstractScreenOptions<T>>
    extends AbstractPage<T> {

  private static final String NEW_SCREEN_OPTIONS = "screenOptions";
  private static final String OLD_SCREEN_OPTIONS = "bluebar_screenoptions";
  private static final String NEW_SCREEN_OPTIONS_BUTTON = "screenOptionsOpen";
  private static final String OLD_SCREEN_OPTIONS_BUTTON = "bluebar_screenoptions_button";
  // property used to help pass EquellaConnectorTest in new UI
  private boolean isConnectorTest = false;

  public AbstractScreenOptions(PageContext context) {
    super(context);
    loadedBy = By.id(getOptionsId());
  }

  protected String getOptionsId() {
    return isNewUI() && !isConnectorTest ? NEW_SCREEN_OPTIONS : OLD_SCREEN_OPTIONS;
  }

  private By getOpenOptionsBy() {
    if (!isNewUI() || isConnectorTest) {
      return By.id(OLD_SCREEN_OPTIONS_BUTTON);
    } else {
      return By.id(NEW_SCREEN_OPTIONS_BUTTON);
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

  public void setupForTest() {
    isConnectorTest = true;
    loadedBy = By.id(getOptionsId());
  }

  public void close() {
    if (isOptionsOpen()) {
      if (isNewUI() && !isConnectorTest) {
        WebElement element =
            driver.findElement(
                By.xpath("//div[div/div[@id = " + quoteXPath(getOptionsId()) + "]]"));
        driver.findElement(By.xpath("//body")).click();
        waiter.until(w -> "hidden".equals(element.getCssValue("visibility")));
      } else {
        ExpectedCondition<Boolean> invisible =
            ExpectedConditions.invisibilityOfElementLocated(loadedBy);
        getOpenOptions().click();
        waiter.until(invisible);
      }
    }
  }
}
