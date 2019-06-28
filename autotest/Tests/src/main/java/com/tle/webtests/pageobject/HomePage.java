package com.tle.webtests.pageobject;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.portal.AbstractPortalEditPage;
import com.tle.webtests.pageobject.portal.MenuSection;
import com.tle.webtests.pageobject.portal.PortalScreenOptions;
import org.openqa.selenium.By;

public class HomePage extends AbstractPage<HomePage> {
  public HomePage(PageContext context) {
    super(context, By.xpath("//div[contains(@class, 'dashboard')]"));
  }

  public boolean portalExists(String title) {
    return isPresent(By.xpath("//h3[normalize-space(text())=" + quoteXPath(title) + "]"));
  }

  @Override
  protected void loadUrl() {
    driver.get(context.getBaseUrl() + "home.do");
  }

  public boolean containsLink(String name, String url) {
    MenuSection ms = new MenuSection(context).get();
    return ms.linkExists(name, url);
  }

  private PortalScreenOptions openScreenOptions() {
    return new PortalScreenOptions(context).open();
  }

  public <P extends AbstractPortalEditPage<P>> P addPortal(P portal) {
    return new PortalScreenOptions(context).open().addPortal(portal);
  }

  public HomePage restoreAll() {
    openScreenOptions().restoreAll();
    return new HomePage(context).get();
  }

  public boolean isTopicTagVisible(String dynamicTopicName) {
    MenuSection ms = new MenuSection(context).get();
    return ms.hasMenuOption(dynamicTopicName);
  }
}
