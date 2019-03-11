package com.tle.webtests.pageobject.portal;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.ExpectedConditions2;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class FreemarkerPortalSection extends AbstractPortalSection<FreemarkerPortalSection> {
  public FreemarkerPortalSection(PageContext context, String title) {
    super(context, title);
  }

  public boolean scriptCountdownTest(String spanId) {
    WebElement span = driver.findElement(By.id(spanId));
    try {
      waiter.until(ExpectedConditions2.elementTextToBe(span, "finished!"));
      return true;
    } catch (Exception e) {
      return false;
    }
  }
}
