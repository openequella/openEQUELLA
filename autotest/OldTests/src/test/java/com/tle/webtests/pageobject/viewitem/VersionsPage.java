package com.tle.webtests.pageobject.viewitem;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class VersionsPage extends AbstractPage<VersionsPage> {
  public VersionsPage(PageContext context) {
    super(context, By.xpath("//h2[normalize-space(text())='Versions of this item']"));
  }

  public SummaryPage clickVersion(int version) {
    driver
        .findElement(
            By.xpath(
                "//table[contains(@class, 'zebra')]//td[normalize-space(text())="
                    + quoteXPath(version)
                    + "]/following-sibling::td/a"))
        .click();
    return new SummaryPage(context).get();
  }

  public String getStatusByVersion(int version) {
    WebElement versionRow =
        driver.findElement(
            By.xpath("//table[@id='vc_v']/tbody/tr[./td[1][text()='" + version + "']]/td[3]"));
    return versionRow.getText();
  }

  public String getNameByVersion(int version) {
    WebElement versionRow =
        driver.findElement(
            By.xpath("//table[@id='vc_v']/tbody/tr[./td[1][text()='" + version + "']]/td[2]"));
    return versionRow.getText();
  }
}
