package com.tle.webtests.pageobject;

import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class MUIHelper {

  public static String getBadgeText(WebElement base) {
    List<WebElement> elems = base.findElements(By.xpath("./button/span/span/span"));
    if (elems.isEmpty()) {
      return null;
    }
    return elems.get(0).getText();
  }
}
