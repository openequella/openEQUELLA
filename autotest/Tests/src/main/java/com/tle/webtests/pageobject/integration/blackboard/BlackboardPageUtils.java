package com.tle.webtests.pageobject.integration.blackboard;

import com.tle.webtests.pageobject.AbstractPage;
import org.openqa.selenium.By;

public class BlackboardPageUtils {

  public static By pageTitleBy(String title) {
    return By.xpath(
        "id('pageTitleBar')/h1/span[normalize-space(text())="
            + AbstractPage.quoteXPath(title)
            + "]");
  }
}
