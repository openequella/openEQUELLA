package com.tle.webtests.pageobject;

import com.tle.webtests.framework.PageContext;
import org.openqa.selenium.By;

public class AccessDeniedErrorPage extends ErrorPage {

  public AccessDeniedErrorPage(PageContext context) {
    super(context);
  }

  public String getDenied() {
    return driver.findElement(By.xpath("//div[@class='area error']/p[@id='denied']")).getText();
  }
}
