package com.tle.webtests.pageobject.reporting;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractReport;
import org.openqa.selenium.By;

public class ConnectorReportPage extends AbstractReport<ConnectorReportPage> {
  public ConnectorReportPage(PageContext context) {
    super(context, By.xpath("//div[normalize-space(text())='External Title']"));
  }

  public boolean hasResource(String title, String course) {
    return isVisible(
        By.xpath(
            "id('__bookmark_1')//tr/td[1]/div[descendant::text()="
                + quoteXPath(title)
                + "]/../../td[2]/descendant::*[contains(text(), "
                + quoteXPath(course)
                + ")]"));
  }
}
