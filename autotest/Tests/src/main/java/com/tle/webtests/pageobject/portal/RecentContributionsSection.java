package com.tle.webtests.pageobject.portal;

import com.tle.webtests.framework.PageContext;
import org.openqa.selenium.By;

public class RecentContributionsSection extends AbstractPortalSection<RecentContributionsSection> {

  public RecentContributionsSection(PageContext context, String title) {
    super(context, title);
  }

  public boolean recentContributionExists(String itemName) {
    return isPresent(
        getBoxContent(),
        By.xpath(
            ".//div[normalize-space(@class)='recent-items']//a[normalize-space(text())="
                + quoteXPath(itemName)
                + "]"));
  }

  public boolean descriptionExists(String description) {
    return isPresent(
        getBoxContent(),
        By.xpath(
            ".//div[normalize-space(@class)='recent-items']//p[normalize-space(text())="
                + quoteXPath(description)
                + "]"));
  }
}
