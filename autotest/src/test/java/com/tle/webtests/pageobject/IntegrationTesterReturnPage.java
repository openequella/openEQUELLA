package com.tle.webtests.pageobject;

import com.tle.webtests.framework.PageContext;
import org.openqa.selenium.By;

public class IntegrationTesterReturnPage extends AbstractPage<IntegrationTesterReturnPage> {

  public IntegrationTesterReturnPage(PageContext context) {
    super(context, By.xpath("//span[text()='IntegTester Return Info']"));
  }

  public String getReturnedUrl() {
    return returnedRow("url");
  }

  public String returnedRow(String row) {
    return driver.findElement(By.xpath("//textarea[@name='" + row + "']")).getAttribute("value");
  }

  public boolean isSuccess() {
    return isPresent(By.xpath("//textarea[text()='success']"));
  }
}
