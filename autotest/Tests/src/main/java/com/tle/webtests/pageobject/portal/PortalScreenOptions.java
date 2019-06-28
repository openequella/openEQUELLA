package com.tle.webtests.pageobject.portal;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.generic.page.AbstractScreenOptions;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class PortalScreenOptions extends AbstractScreenOptions<PortalScreenOptions> {
  @FindBy(id = "ri_restoreAllButton")
  private WebElement restoreAll;

  public PortalScreenOptions(PageContext context) {
    super(context);
  }

  public <P extends AbstractPortalEditPage<P>> P addPortal(P portal) {
    By portalLink =
        By.xpath(
            "id("
                + quoteXPath(getOptionsId())
                + ")/ul/li/a[text()="
                + AbstractPage.quoteXPath(portal.getType())
                + "]");
    waiter.until(ExpectedConditions.elementToBeClickable(portalLink)).click();
    return portal.get();
  }

  public void restoreAll() {
    restoreAll.click();
  }
}
