package com.tle.webtests.framework;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;

public final class TestUtils {
  private TestUtils() {
    throw new UnsupportedOperationException("Utility class");
  }

  /**
   * Forcefully clicks a button using executeScript for when normal Element.click() fails for
   * unknown reasons (e.g., Element is unexpectedly stale).
   *
   * @param executor often the WebDriver for a Page, or otherwise the result of Context.getDriver()
   * @param button WebElement to be clicked
   */
  public static void forceButtonClickWithJS(JavascriptExecutor executor, WebElement button) {
    executor.executeScript("arguments[0].click();", button);
  }
}
