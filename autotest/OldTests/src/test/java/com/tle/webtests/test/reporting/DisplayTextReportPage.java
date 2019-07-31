package com.tle.webtests.test.reporting;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractReport;
import org.openqa.selenium.By;

public class DisplayTextReportPage extends AbstractReport<DisplayTextReportPage> {
  public DisplayTextReportPage(PageContext context) {
    super(
        context,
        By.xpath(
            "//div[text()='Param #1, should have the collection id (a number), then the name']"));
  }

  // Zero indexed
  public String getReportValue(int index, boolean displayText) {
    return driver
        .findElement(
            By.xpath("//tbody//tr[2]/td/span[" + (index * 4 + 2 + (displayText ? 2 : 0)) + "]"))
        .getText();
  }
}
