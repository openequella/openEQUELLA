package com.tle.webtests.pageobject.portal;

import com.tle.webtests.framework.PageContext;
import org.openqa.selenium.By;

public class HtmlPortalSection extends AbstractPortalSection<HtmlPortalSection> {
  public HtmlPortalSection(PageContext context, String title) {
    super(context, title);
  }

  public String portalText() {
    return getBoxContent().findElement(By.xpath("div/p")).getText();
  }
}
