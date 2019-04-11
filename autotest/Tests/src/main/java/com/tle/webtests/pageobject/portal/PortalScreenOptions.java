package com.tle.webtests.pageobject.portal;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.generic.page.AbstractScreenOptions;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class PortalScreenOptions extends AbstractScreenOptions<PortalScreenOptions> {
  @FindBy(id = "ri_restoreAllButton")
  private WebElement restoreAll;

  public PortalScreenOptions(PageContext context) {
    super(context);
  }

  public <P extends AbstractPortalEditPage<P>> P addPortal(P portal) {
    By portalLink =
        By.xpath(
            "id('bluebar_screenoptions')/ul/li/a[text()="
                + AbstractPage.quoteXPath(portal.getType())
                + "]");
    waitForElement(portalLink).click();
    return portal.get();
  }

  public void restoreAll() {
    restoreAll.click();
  }
}
