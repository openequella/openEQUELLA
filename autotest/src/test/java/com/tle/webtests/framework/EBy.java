package com.tle.webtests.framework;

import com.tle.webtests.pageobject.AbstractPage;
import org.openqa.selenium.By;

public final class EBy {

  private EBy() {
    throw new UnsupportedOperationException();
  }

  public static By buttonText(String text) {
    return By.xpath(
        ".//button[normalize-space(child::text()) = " + AbstractPage.quoteXPath(text) + "]");
  }

  public static By linkText(String text) {
    return By.xpath(".//a[normalize-space(child::text()) = " + AbstractPage.quoteXPath(text) + "]");
  }
}
