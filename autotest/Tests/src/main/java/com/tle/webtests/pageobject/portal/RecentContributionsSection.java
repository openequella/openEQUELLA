package com.tle.webtests.pageobject.portal;

import com.tle.webtests.framework.PageContext;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class RecentContributionsSection extends AbstractPortalSection<RecentContributionsSection> {

  public RecentContributionsSection(PageContext context, String title) {
    super(context, title);
  }

  public boolean recentContributionExists(String itemName) {
    waiter.until(
        ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//a[normalize-space(text())=" + quoteXPath(itemName) + "]")));
    return isPresent(
        getBoxContent(),
        By.xpath(
            ".//div[normalize-space(@class)='recent-items']//a[normalize-space(text())="
                + quoteXPath(itemName)
                + "]"));
  }

  public boolean descriptionExists(String description, boolean expectedExisting) {
    String descriptionQuoteXpath = quoteXPath(description);
    if (expectedExisting) {
      waiter.until(
          ExpectedConditions.visibilityOfElementLocated(
              By.xpath("//p[normalize-space(text())=" + descriptionQuoteXpath + "]")));
    }
    return isPresent(
        getBoxContent(),
        By.xpath(
            ".//div[normalize-space(@class)='recent-items']//p[normalize-space(text())="
                + descriptionQuoteXpath
                + "]"));
  }
}
