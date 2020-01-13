package com.tle.webtests.pageobject.portal;

import com.tle.webtests.framework.PageContext;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class RecentContributionsSection extends AbstractPortalSection<RecentContributionsSection> {

  public RecentContributionsSection(PageContext context, String title) {
    super(context, title);
  }

  public boolean recentContributionExists(String itemName) {
    By recentContributionXpath =
        By.xpath(
            ".//div[normalize-space(@class)='recent-items']//a[normalize-space(text())="
                + quoteXPath(itemName)
                + "]");
    waiter.until(ExpectedConditions.visibilityOfElementLocated(recentContributionXpath));
    return isPresent(recentContributionXpath);
  }

  public boolean descriptionExists(String description, boolean expectedExisting) {
    By descriptionQuoteXpath =
        By.xpath("//p[normalize-space(text())=" + quoteXPath(description) + "]");
    if (expectedExisting) {
      waiter.until(ExpectedConditions.visibilityOfElementLocated(descriptionQuoteXpath));
    }
    return isPresent(descriptionQuoteXpath);
  }
}
