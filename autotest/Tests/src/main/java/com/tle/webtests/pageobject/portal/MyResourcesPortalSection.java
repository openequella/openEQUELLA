package com.tle.webtests.pageobject.portal;

import com.tle.webtests.framework.PageContext;
import org.openqa.selenium.By;

public class MyResourcesPortalSection extends AbstractPortalSection<MyResourcesPortalSection> {
  public MyResourcesPortalSection(PageContext context, String title) {
    super(context, title);
  }

  public void clickLink(String title) {
    getBoxContent().findElement(By.xpath("div/a[text()=" + quoteXPath(title) + "]")).click();
  }
}
