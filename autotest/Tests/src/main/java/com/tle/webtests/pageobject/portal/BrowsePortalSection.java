package com.tle.webtests.pageobject.portal;

import com.tle.webtests.framework.PageContext;
import org.openqa.selenium.By;

public class BrowsePortalSection extends AbstractPortalSection<BrowsePortalSection> {
  public BrowsePortalSection(PageContext context, String title) {
    super(context, title);
  }

  public boolean topicExists(String topic) {
    return isPresent(
        getBoxContent(),
        By.xpath(".//ul[@class='topics']/li/a[starts-with(., " + quoteXPath(topic) + ")]"));
  }
}
